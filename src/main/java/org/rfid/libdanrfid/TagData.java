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
	protected char[] forward32 = new char[32]; // represents the tag user fields
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

	/**
	 * constructor from byte[] Array
	 * @param in - userdata as bytes read from tag
	 */
	public TagData(byte[] in){
		initdata(in);
	}

	/**
	 * constructor from Byte[]
	 * @param array - the Byte Object array 
	 * @return 
	 */
	public TagData(Byte[] array) {
		byte[] ret=new byte[array.length];
		for(int i=0;i<array.length;i++){
			ret[i]=array[i].byteValue();
		}
		initdata(ret);
	}

	 /**
	  * constructor from Hex String as from toString()
	  * @param ins - 32 byte userdata as read from the tag, an optional "0x" prefix will be ignored
	  */
	public TagData(String ins){
		int startat=0;
		ins.toLowerCase(); // ignore case
		if(ins.startsWith("0x")){ // lets believe in HEX
			startat=2;
		}
	
		String hex=ins.substring(startat); // drop the leading 0x
		
		initdata(Util.hexStringToByteArray(hex));
	}
	
	/**
	 * provide an initialized Tag
	 */
	public TagData() {
		super();
		keepshadowdata();
	}

	/**
	 * DDMData will verify the CRC
	 * and possibly revert the block byte order(!)
	 * @return true as default here
	 */
	public boolean isvalid() {
		return true;
			}

	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index
	 * @return a byte array with the block 
	 * TODO : with respect to ordering? ;-)
	 */
	public byte[] getblock(int n) {
		return getblock(n,4,false); // default to blocksize 4
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
	 * @param n
	 * @return true if block needs to be written back to tag
	 */
	public boolean blocktainted(int n) {
		return blocktainted(n,4);
	}

	/**
	 * find out if our updates have changed block n
	 * @param n
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
	 * pre-set the (32 Byte) userdate 
	 * @param in - the byte[] to be set
	 */
	public void addUserData(byte[] in) {
		initdata(in);
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
	 * initialize the 32 byte user data array
	 * @param in - a byte array as read from
	 */
	protected void initdata(byte[] in) {
		for (int i = 0; i < 32; i++) { // dont trust in length ;-) 
			if(i<in.length) // any case..
				userdata32[i]=(char)in[i];
			else
				userdata32[i]=(char)0; // init all
		}
		// is this a crc ok Record?
		// nb: reverts the block byte order, if necessary
		foundCRCok=isvalid();
		
		keepshadowdata(); // keep this state in order to track the changes		
	}

	/**
	 * clone userdata32 into char[32] shadowdata
	 */
	protected void keepshadowdata() {
		shadowdata=userdata32.clone();
		for(int i=0;i<userdata32.length;i++)
			shadowdata[i]=(char)(userdata32[i]&0xff); // chars may have a sign?
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
	 * extract the barcode to a string
	 * @return
	 */
	public String Barcode() {
		// TODO Auto-generated method stub
		return "dummyBarcode";
	}

	/**
	 * fill in the Barcode
	 * @param bc
	 */
	public void setBarcode(String bc) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return - the countrycode
	 * read from Tag, if possible
	 */
	public String Country(){
		return myCountry;	
	}
	
	/**
	 * fill in the countrycode
	 * @param iso
	 */
	public void setCountry(String iso){
	}
	
	/**
	 * extract the ISIL code from Tag, if it is thete
	 * @return - the ISIL as String
	 */
	public String ISIL(){
			return myISIL;

	}
	/**
	 * fill in the ISIL
	 * @param iso
	 */
	public void setISIL(String s){
	}

	/**
	 * HEX representation of the 32 byte payload of a tag
	 * with the CRC updated
	 */
	public String toString(){
		String res="";
		updateCRC(); // the CRC is refreshed automagically before giving away the content
		for (char d : userdata32) {
			res=res.concat(String.format("%02x", (byte)d));			
		}
		return res;
	}
	
	public int updateCRC() {
		// TODO Auto-generated method stub
		return 0;
	}
	

}
