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
public class DDMData extends TagData{
	
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
		
		TagData td=new DDMData();
		td.setBarcode(args[0]);
		System.out.format("%s", td.toString());
		
	}
	
	public static void usage(){
		System.out.format("usage: $0 <barcode>\n");
		System.out.format("will return the 32 byte userdata of a RFID Tag\n");
	}

	/**
	 * constructor from byte[] Array
	 * @param in - userdata as bytes read from tag
	 */
	public DDMData(byte[] in){
        super(TAG_DDM,in);
	}

	/**
	 * constructor from Byte[]
	 * @param array - the Byte Object array 
	 * @return 
	 */
	public DDMData(Byte[] array) {
		super(TAG_DDM,array);
	}

	/**
	 * constructor from Hex String as from toString()
	 * @param ins - 32 byte userdata as read from the tag, an optional "0x" prefix will be ignored
	 */
	public DDMData(String ins){
		super(TAG_DDM,ins);
	}
	
	/**
	 * provide an initialized Tag
	 * - single Medium
	 * - County and ISIL initialized with default values (hard coded)
	 */
	public DDMData(){
//		shadowdata=userdata32.clone(); // keep empty shadow in order to mark all blocks tainted
		addUserData(new byte[32]);
		keepshadowdata();
		setUsage(V1AUSLEIHBAR);
		setofParts(1);
		setPartNum(1);
		setCountry(myCountry);
		setISIL(myISIL);		
	}

	/**
	 * rfresh the CRC at the given position
	 * @return the int value of the new CRC 
	 */
	@Override
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
	
	@Override
	public String Barcode(){
		String res=new String(userdata32, 3, 16);
		int i = res.indexOf(0);
		if(i>0 && i<16)
			return res.substring(0, res.indexOf(0));
		else
			return res;
	}
	
	@Override
	public void setBarcode(String bc){
		setStringAt(bc, 3, 16);
	}
	
	
	@Override
	public String Country(){
		return new String(userdata32,21,2);
	}
	
	@Override
	public void setCountry(String iso){
		setStringAt(iso, 21, 2);
	}
	
	@Override
	public String ISIL(){
		String res= new String(userdata32,23,9); // the far end, even when ISIL allow for 11 chars
		int i = res.indexOf(0);
		if(i>0 && i<9)
			return res.substring(0, res.indexOf(0));
		else
			return res;
	}

	@Override
	public void setISIL(String s){
		setStringAt(s, 23, 9);
	}
	
}
