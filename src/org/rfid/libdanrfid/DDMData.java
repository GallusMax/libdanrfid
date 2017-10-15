/*
  	Copyright (C) 2011 Ulrich Hahn
  	
  	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.rfid.libdanrfid;

/**
 * A representation of the mandatory fields within the Danish Data Format
 * found on the RFID Tags of some Libraries
 * 
 * @author uhahn
 *
 */
public class DDMData extends Object{
	/*
	 * These presets are used when a blank record is created
	 * customize here - ugly, too!
	 */
	public final String myCountry="DE";
	public final String myISIL="705";
	
	/* one byte combines Version and media usage 
	 * this is as ugly a these constants
	 */
	public static final char V1NEU=16; 		// 0x10
	public static final int USAGE_NEU=0; 		// the usage nibble (the low for FKI, the high for bibliotheca) 
	public static final char V1AUSLEIHBAR=17; 	// 0x11
	public static final int USAGE_AUSLEIHBAR=1; 
	public static final char V1PRAESENZ=18; 	// 0x12
	public static final int USAGE_PRAESENZ=2; 	// 0x12
	public static final char V1LOCAL3=19; 	// 0x13 "ISO_FDIS_28560-3"
	public static final int USAGE_3=3; 		// 0x13 "ISO_FDIS_28560-3"
	public static final char V1LOCAL4=20; 	// 0x14
	public static final int	USAGE_4=4; 		// 0x14
	public static final char V1FUTURE=21; 	// 0x15
	public static final int	USAGE_5=5; 		// 0x15
	public static final char V1UNKNOWN=22; 	// 0x16
	public static final int	USAGE_6=6; 		// 0x16
	public static final char V1GELOESCHT=23;  	// 0x17
	public static final int	USAGE_GELOESCHT=7; 	// 0x17
	public static final char V1KUNDE=24; 	// 0x18
	public static final int	USAGE_KUNDE=8; 	// 0x18
	public static final char V1EQUIPMENT=0x19; // 0x19
	public static final int	USAGE_EQUIPMENT=9; // 0x19
	public static final char V2NEU=0x20;
	public static final char V2AUSLEIHBAR=0x21;
	public static final char BIBLIOTHECA_PRAESENZ=0x21;
	public static final char BIBLIOTHECA_GELOESCHT=0x71;
	public final static String[] TypeName = {"Neuerwerbung","Ausleihbar","Präsenz","local 0x13","local 0x14","future 0x15","Unbekannt","Gelöscht","Kundenkarte","Gerät"};

//	public final static Hash<char,String> HStatus=new Hash<char,String>{};
	
	/*
	 * TODO this schould be the real block properties from Tag
	 */
	protected int blocksize=4;
	protected int nBlocks=8; // number of blocks on tag - not the DDM size!
	protected int nDDMblocks=8; // fits into userdata32..
	
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
	 protected boolean reversed=false;  
	
	 /**
	  * remember if the tag had a valid CRC weh we read it
	  */
	protected boolean foundCRCok=false;
	/**
	 * the DDM data format version, used on status updates
	 */
	private char VERSION=1;
	
	/**
	 * @param args
	 * usage: $0 <barcode> [more args?]
	 * prints out the 32 byte Tag user data 
	 * complete with CRC according Daenisches Datenmodell
	 */
	public static void main(String[] args) {

		if(0==args.length){
			usage();
			return;
		}
		
		DDMData td=new DDMData();
		td.setBarcode(args[0]);
		System.out.format("%s", td.toString());
		
	}
	
	public static void usage(){
		System.out.format("usage: $0 <barcode>\n");
		System.out.format("will return the 32 byte userdata of a RFID Tag\n");
	}

	
	/**
	 * initialize the 32 byte user data array
	 * @param in - a byte array as read from
	 */
	protected void initdata(byte[] in)
	{
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
	
	private void keepshadowdata(){
		shadowdata=userdata32.clone();
		for(int i=0;i<userdata32.length;i++)
			shadowdata[i]=(char)(userdata32[i]&0xff); // chars may have a sign?
	}
	/**
	 * compares a single byte between current array and original array
	 * @param i
	 * @return true if byte i was changed
	 */
	public boolean bytetainted(int i){
		boolean istainted = userdata32[i]!=shadowdata[i];
		if(istainted)
			return istainted;
		else
			return istainted;
	}
	
	/**
	 * constructor from byte[] Array
	 * @param in - userdata as bytes read from tag
	 */
	public DDMData(byte[] in){
		initdata(in);
	}

	/**
	 * constructor from Byte[]
	 * @param array - the Byte Object array 
	 * @return 
	 */
	public DDMData(Byte[] array) {
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
	public DDMData(String ins){
		int startat=0;
		ins.toLowerCase(); // ignore case
		if(ins.startsWith("0x")){ // lets believe in HEX
			startat=2;
		}
	
		String hex=ins.substring(startat); // drop the leading 0x
		
		initdata(Util.hexStringToByteArray(hex));
/*		
		ArrayList<Byte> bal=new ArrayList<Byte>();
		
		  //49204c6f7665204a617661 split into two characters 49, 20, 4c...
		  for( int i=0; i<hex.length()-1; i+=2 ){
	 
		      //grab the hex in pairs
		      String output = hex.substring(i, (i + 2));
		      //convert hex to decimal
		      int decimal = Integer.parseInt(output, 16);
		      //convert the decimal to character
		      bal.add((byte)decimal);
		  }
		  
	  bal.trimToSize();
//	  DDMData((Byte[])bal.toArray());
	  Object[] array= (bal.toArray());

	  // copied helplessly
		byte[] in=new byte[array.length];
		for(int i=0;i<array.length;i++){
			in[i]=((Byte)(array[i])).byteValue();
		}
		initdata(in);
*/	
	}
	
	/**
	 * provide an initialized Tag
	 * - single Medium
	 * - County and ISIL initialized with default values (hard coded)
	 */
	public DDMData(){
		shadowdata=userdata32.clone(); // keep empty shadow in order to mark all blocks tainted
		setUsage(V1AUSLEIHBAR);
		setofParts(1);
		setPartNum(1);
		setCountry(myCountry);
		setISIL(myISIL);		
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
	
	/**
	 * rfresh the CRC at the given position
	 * @return the int value of the new CRC 
	 */
	public int updateCRC(){
		int crc_sum=new CRC().DDCRC(userdata32);

		// replace existing CRC in given array
		if(userdata32.length<21)return crc_sum; // dont step beyond - shouldnt happen
		userdata32[19]=(char)(crc_sum & 0xff); // lsb first
		int msb=crc_sum ;
		msb>>=8;
		userdata32[20]=(char)(msb & 0xff);
		return crc_sum;
	}
	
	/**
	 * check the given CRC 
	 * @return false if CRC do not match
	 */
	public boolean compareCRC(){
		int crc_sum=new CRC().DDCRC(userdata32);
		int found_crc=getCRC();
		return crc_sum==found_crc;
	}
	
	/**
	 * try to verify the CRC
	 * first try with given block order
	 * then reverse each 4 byte block and try again
	 * @return false if both block directions do not produce a valid CRC
	 * the block direction will be reverted again, if both do not match
	 */
	public boolean isvalid(){
		if(compareCRC())return true;
		reverseblocks();
		if(compareCRC())return true;
		reverseblocks(); // back again
		return false;
	}
	
	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index
	 * @return a byte array with the block 
	 * TODO : with respect to ordering? ;-)
	 */
	public byte[] getblock(int n){
		return getblock(n,4,false); // default to blocksize 4
	}

	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index 
	 * @param forcereverse requests reverted byteorder (on empty tags)
	 * @return a byte array with the block 
	 */
	public byte[] getblock(int n, boolean forcereverse){
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
	public byte[] getblock(int n, int blocksize, boolean forcereverse){
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
	public boolean blocktainted(int n){
		return blocktainted(n,4);
	}
	
	/**
	 * find out if our updates have changed block n
	 * @param n
	 * @param blocksize
	 * @return true if block needs to be written back to tag
	 */
	public boolean blocktainted(int n, int blocksize){
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
	public void reverseblocks(){ // TODO use actual blocksizes from tag system data
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
	public char[] reverse(char[] in, int offset, int block){
		char bt;
		for (int i = 0; i < block/2; i++) {
			bt=in[offset+i];
			in[offset+i]=in[offset+block-i-1];
			in[offset+block-i-1]=bt;
		}
		return in;
	}

	
	/**
	 * read the CRC as int, added up LSB first, MSB last
	 * @return
	 */
	public int getCRC(){
		int res=userdata32[20]&0xff;
		res<<=8;
		res+=userdata32[19]&0xff;
		return res;
	}
	
	public int getofParts(){
		return (int)userdata32[1]&0xff;
	}

	/**
	 * change the number of parts 
	 * @param n - the number of parts
	 */
	public void setofParts(int n){
		userdata32[1]=(char)n;
	}
	
	public int getPartNum(){
		return (int)userdata32[2]&0xff;
	}

	/**
	 * change the item number to @param i of ofParts
	 * @param i 
	 */
	public void setPartNum(int i){
		userdata32[2]=(char)i;
		// TODO update "ofParts" in a plausible way
	}
	
	/**
	 * change the usage (aka media status)
	 * @param usage - either the usage char or the usage nibble alone
	 * n.b.: the version is set to default version, if usage value < 0x10 
	 * TODO: this currently doesnt work on Bibliotheca
	 */
	public void setUsage(char usage){
		userdata32[0]=(char) ((0<(usage&0xf0)) ? usage : usage&(VERSION<<4));
	}

	/**
	 * aka media status nibble
	 * TODO this will currently (silently) fail on Bibliotheca (and other?) tags,
	 * as have another nibble order. silently means the version nibble (currently 1) 
	 * is returned as the most common media status "Ausleihbar"
	 * @return the usage nibble - without the version!
	 * compare the result with final int values USAGE_..
	 */
	public int getUsage(){
		return userdata32[0]&0x0f;
	}
	
	/**
	 * 
	 * @return the media status in a readable form
	 * TODO localization per platform?
	 */
	public String getUsageAsString(){
		String res=String.format("0x%x", getUsage());
		try{
			res = TypeName[getUsage()];
		}
		catch (IndexOutOfBoundsException e){
		}
		return res;
	}
	/**
	 * 
	 * @return the raw byte containing 
	 * a) nibble VERSION
	 * b) nibble Tag Usage Code
	 */
	public char getcharVersionUsage(){
		return userdata32[0];
	}
	public byte getVersionUsage(){
		return (byte)(userdata32[0]&0xff);
	}
	/**
	 * extract the barcode to a string
	 * @return
	 */
	public String Barcode(){
		String res=new String(userdata32, 3, 16);
		int i = res.indexOf(0);
		if(i>0 && i<16)
			return res.substring(0, res.indexOf(0));
		else
			return res;
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
	private void setStringAt(String s,int start, int len){
		for (int i = 0; i < len; i++) {
			if(i>=s.length()) // well - we fill in \0 here..
				userdata32[start+i]='\0';	
			else
				userdata32[start+i]=s.charAt(i);			
		}				
	}
	/**
	 * fill in the Barcode
	 * @param bc
	 */
	public void setBarcode(String bc){
		setStringAt(bc, 3, 16);
	}
	
	
	/**
	 * extract the countrycode
	 * @return
	 */
	public String Country(){
		return new String(userdata32,21,2);
	}
	
	/**
	 * fill in the countrycode
	 * @param iso
	 */
	public void setCountry(String iso){
		setStringAt(iso, 21, 2);
	}
	
	/**
	 * extract the ISIL code
	 * @return
	 */
	public String ISIL(){
		String res= new String(userdata32,23,9); // the far end, even when ISIL allow for 11 chars
		int i = res.indexOf(0);
		if(i>0 && i<9)
			return res.substring(0, res.indexOf(0));
		else
			return res;

	}
	/**
	 * fill in the ISIL
	 * @param iso
	 */
	public void setISIL(String s){
		setStringAt(s, 23, 9);
	}
	
	/**
	 * 
	 * @return the blocksize according to Tag system info
	 */
	public int getblocksize(){
		return (int)blocksize;
	}
	
	/**
	 * 
	 * @return the number of blocks on the Tag
	 */
	public int getnBlocks(){
		return (int)nBlocks;
	}

	public void addUserData(byte[] in) {
		initdata(in);
	}
	

	
}
