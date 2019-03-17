package org.rfid.libdanrfid;

public class TagData extends Object {
	/*
	 * These presets are used when a blank record is created
	 * customize here - ugly, too!
	 */
	public final String myCountry="DE";
	public final String myISIL="705";

	/*
	 * TODO this schould be the real block properties from Tag
	 */
    protected int blocksize = 4;
	protected int nBlocks = 8;
	protected int nDDMblocks = 8;
	
	/*
	 * some like it forward, some reverse
	 * still looking for a performant way of handling this
	 * a cure would be a reference implementation 
	 * 
	 */
//	protected char[] forward32 = new char[32]; // represents the tag user fields
	protected char[] forward32 = null; // set in subclass represents the tag user fields
//	protected char[] reverse32 = new char[32]; // reversed block order
	protected char[] userdata32=forward32;
	protected char[] shadowdata; // keep array as initialized for change tracking
	/**
	 * remember if we found reverted byteorder on tag
	 */
	protected boolean reversed = false;
	/**
	  * remember if the tag had a valid CRC when we read it
	  */
	protected boolean foundCRCok = false;

	public final static int TAG_UNKNOWN=0;
	public final static int TAG_DDM=1;
	public final static int TAG_DM11=2;
	protected int tagType=TAG_UNKNOWN; // initially unknown
	
	DDMData ddmInstance;
	DM11Data dm11Instance;


	public TagData(int tagHint,byte[] in){
		tagType = tagHint;
		if(TAG_UNKNOWN == tagType)
			createInstances(in);

		addUserData(in);
	}

	/**
	 * constructor from byte[] Array
	 * @param in - userdata as bytes read from tag
	 */
	public TagData(byte[] in){
		this(TAG_UNKNOWN,in);
//		createInstances(in);
	}

	public TagData(int tagHint, Byte[] array) {
		tagType=tagHint;
		byte[] ret=new byte[array.length];
		for(int i=0;i<array.length;i++){
			ret[i]=array[i].byteValue();
		}
		addUserData(ret);
	}

	/**
	 * constructor from Byte[]
	 * @param array - the Byte Object array 
	 * @return 
	 */
	// TODO Byte[] (needed?)
//	public TagData(Byte[] array) {
////		this(TAG_UNKNOWN,array);
//		initdata(array);
//	}
		
	public TagData(int tagHint, String ins) {
		tagType=tagHint;
		if(TAG_UNKNOWN == tagType)
			createInstances(Util.hexStringToByteArray(ins));

		addUserData(Util.hexStringToByteArray(ins));

	}

	 /**
	  * constructor from Hex String as from toString()
	  * @param ins - 32 byte userdata as read from the tag, an optional "0x" prefix will be ignored
	  */
	public TagData(String ins){
		this(TAG_UNKNOWN,ins);
	}
	
	/**
	 * provide an initialized Tag
	 */
	public TagData() {
//		super();
//		if(null==userdata32) userdata32 = new char[32];
		createInstances(new byte[32]);
//		keepshadowdata();
	}

	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index
	 * @return a byte array with the block 
	 * TODO : with respect to ordering? ;-)
	 */
	public byte[] getblock(int n) {
		return getblock(n,4,false); // default to blocksize 4 TODO: real blocksize
	}

	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index 
	 * @param forcereverse requests reverted byteorder (on empty tags)
	 * @return a byte array with the block 
	 */
	public byte[] getblock(int n, boolean forcereverse) {
		return getblock(n,4,forcereverse); // default to blocksize 4
	}

	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index 
	 * @param blocksize - respect tag's blocksize
	 * @param forcereverse requests reverted byteorder (on empty tags)
	 * @return a byte array with the block 
	 */
	public byte[] getblock(int n, int blocksize, boolean forcereverse) {
		byte[] res= new byte[blocksize];
		
		for(int i=0;i<blocksize;i++)
			res[i]=(byte)((reversed||forcereverse) ? userdata32[(n+1)*blocksize-i-1] : userdata32[n*blocksize+i]);
		return res;
	}

	/**
	 * find out if our updates have changed block n
	 * @param n block index
	 * @return true if block needs to be written back to tag
	 */
	public boolean blocktainted(int n) {
		return blocktainted(n,4);
	}

	/**
	 * find out if our updates have changed block n
	 * @param n block index
	 * @param blocksize
	 * @return true if block needs to be written back to tag
	 */
	public boolean blocktainted(int n, int blocksize) {
		for(int i=0;i<blocksize;i++)
			if(bytetainted(n*blocksize+i)) 
				return true;
		return false;
	}

	/**
	 * change the order of each block 
	 * nb: changes order of blocks in place
	 * property "reversed" keeps track of operations
	 * FKI writes the blocks from 3 to 0, Biblioteca writes 0 to 3
	 */
	public void reverseblocks() { // TODO use actual blocksizes from tag system data
	/*
			int blocksize=4;
			int numblocks=8;
	*/
			for (int k = 0; k < nDDMblocks; k++) { // all blocks
					reverse(userdata32,k*blocksize,blocksize); // swap string positions
			}
			reversed=(reversed?false:true); // toggle reversed marker
		}

	/**
	 * reverse a subset of an array, starting at @param offset 
	 * changing at max @param block bytes
	 * CHANGES THE ARRAY IN PLACE
	 */
	public char[] reverse(char[] in, int offset, int block) {
		char bt;
		for (int i = 0; i < block/2; i++) {
			bt=in[offset+i];
			in[offset+i]=in[offset+block-i-1];
			in[offset+block-i-1]=bt;
		}
		return in;
	}

	/**
	 * 
	 * @return the blocksize according to Tag system info
	 */
	public int getblocksize() {
		return (int)blocksize;
	}

	/**
	 * 
	 * @return the number of blocks on the Tag
	 */
	public int getnBlocks() {
		return (int)nBlocks;
	}

	/**
	 * pre-set the (32 Byte) userdata from a byte[]
	 * @param in - the byte[] to be set
	 */
	public void addUserData(byte[] in) {
		if(null==userdata32) { 
			userdata32 = new char[32];
		}
		for (int i = 0; i < 32; i++) { // dont trust in length ;-) 
			if(i<in.length) // any case..
				userdata32[i]=(char)in[i];
			else
				userdata32[i]=(char)0; // init all
		}

		if(TAG_UNKNOWN == tagType) 
			createInstances(in);
	}
	/**
	 * getter for the one userdata32 char array
	 */
	protected char[] getUserData() {
		switch (tagType) {
		case TAG_DDM:
			return ddmInstance.userdata32;
		case TAG_DM11:
			return dm11Instance.userdata32;
		default:
			return userdata32;
		}  	
	}
	
	/**
	 * compares a single byte between current array and original array
	 * @param i
	 * @return true if byte i was changed
	 */
	public boolean bytetainted(int i) {
		boolean istainted = userdata32[i]!=shadowdata[i];
		if(istainted)
			return istainted;
		else
			return istainted;
	}

	/**
	 * at this point we do not know which tag lies ahead. 
	 * instantiate all known tag models ant check which may be valid
	 * @param in - an unknown byte array as read in
	 */
	private void createInstances(byte[] in) {
			dm11Instance = new DM11Data(in);
			ddmInstance = new DDMData(in);
	
		// rely on CRC check of DDM
			if(ddmInstance.isvalid()) tagType = TAG_DDM;
			else // if this fails, there is a chance of finding a DM11
				if(dm11Instance.isvalid()) tagType = TAG_DM11;
	}


	/**
	 * clone userdata32 into char[32] shadowdata
	 */
	protected void keepshadowdata() {
		shadowdata=userdata32.clone();
//		for(int i=0;i<userdata32.length;i++)
//			shadowdata[i]=(char)(userdata32[i]&0xff); // chars may have a sign?
	}
	
	
	/**
	 * does this Itemcode belong to us? 
	 * @param barcodePattern - the pattern to be checked against
	 * @return - true if matches, or null or empty Pattern given
	 */
	public boolean barcodematch(String barcodePattern){
		if(null==barcodePattern)return true;
		if(barcodePattern.isEmpty()) return true;
		return Barcode().matches(barcodePattern);
	}

	
	/**
	 * put String @param s at postition @param start
	 * with at max @param len characters
	 */
	protected void setStringAt(String s,int start, int len){
		for (int i = 0; i < len; i++) {
			if(i>=s.length()) // well - we fill in \0 here..
				userdata32[start+i]='\0';	
			else
				userdata32[start+i]=s.charAt(i);			
		}				
	}

	/**
	 * @return true. always.
	 */
	public boolean isvalid() {
			return true;
	}

	/**
	 * extract the barcode to a string
	 * @return
	 */
	public String Barcode() {
		switch (tagType){
			case TAG_DDM:
				return ddmInstance.Barcode();

			case  TAG_DM11:
				return dm11Instance.Barcode();
		}

		return "dummyBarcode";
	}

	/**
	 * fill in the Barcode
	 * @param bc
	 */
	public void setBarcode(String bc) {
		switch (tagType){
			case TAG_DDM:
				ddmInstance.setBarcode(bc);
				break;
			case TAG_DM11:
				dm11Instance.setBarcode(bc);
				break;
		}
	}

	/**
	 * read the CRC as int, added up LSB first, MSB last
	 * @return
	 */
	public int getCRC(){
		switch (tagType){
		case TAG_DDM:
			return ddmInstance.getCRC();
		}
		return 0;
	}
	
	public int getofParts() {
		switch (tagType) {
		case TAG_DDM:
			return ddmInstance.getofParts();
		}
		return 0;
	}

	/**
	 * change the number of parts 
	 * @param n - the number of parts
	 */
	public void setofParts(int n){
		switch (tagType) {
		case TAG_DDM:
			ddmInstance.setofParts(n);
			break;
		}
	}
	
	public int getPartNum() {
		switch (tagType) {
		case TAG_DDM:
			return ddmInstance.getPartNum();
		}
		return 0;
	}
	
	/**
	 * change the item number to @param i of ofParts
	 * @param i 
	 */
	public void setPartNum(int i){
		switch (tagType) {
		case TAG_DDM:
			ddmInstance.setPartNum(i);
			break;
		}
	}

	public void setUsage(char newusage) {
		switch (tagType) {
			case TAG_DDM:
				ddmInstance.setUsage(newusage);
				break;
		}
	}

	public int getUsage() {
		return ddmInstance.getUsage();
	}

	/**
	 * @return - the countrycode
	 * read from Tag, if possible
	 */
	public String Country(){
		switch (tagType){
			case TAG_DDM:
				return ddmInstance.Country();

		}
		return myCountry;
	}
	
	/**
	 * fill in the countrycode
	 * @param iso
	 */
	public void setCountry(String iso){
		switch (tagType){
			case TAG_DDM:
				ddmInstance.setCountry(iso);
			break;

		}
	}
	
	/**
	 * extract the ISIL code from Tag, if it is thete
	 * @return - the ISIL as String
	 */
	public String ISIL(){
		switch (tagType){
			case TAG_DDM:
				return ddmInstance.ISIL();

		}
			return myISIL;
	}
	/**
	 * fill in the ISIL
	 * @param isil
	 */
	public void setISIL(String isil){
		switch (tagType){
			case TAG_DDM:
				ddmInstance.setISIL(isil);
			break;

		}

	}
	
	public int updateCRC() {
		switch (tagType){
			case TAG_DDM:
				return ddmInstance.updateCRC();


		}

		return 0;
	}

	/**
	 * HEX representation of the 32 byte payload of a tag
	 * with the CRC updated
	 */
	public String toString() {
		String res="";
		updateCRC(); // the CRC is refreshed automagically before giving away the content
		if(null == userdata32)
			switch(tagType) {
			case TAG_DM11:
				userdata32 = dm11Instance.userdata32;
				break;
				default:
					userdata32 = ddmInstance.userdata32;
		}

		for (char d : userdata32) {
			res=res.concat(String.format("%02x", (byte)d));			
		}
		return res;
	}


}
