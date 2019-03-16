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
 * A representation of the mandatory fields within the DM11 Data Format
 * found on the RFID Tags of other libraries
 * 
 * @author uhahn
 *
 */
public class DM11Data extends TagData{

	private final int[] byteorder = {2,1,3,5,4,6,7};  // order of relevant bytes on chip
	private final int[] presorder = {1,0,3,6,5,4,7,8};  // presentation order of 3-tuples

	public static final String UserDefaultHex="0B41244D54655D18000000000000BB4CA484FF4F42504401500400000000F555";

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
		
		DM11Data td=new DM11Data();
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
	public DM11Data(byte[] in){
		super(TAG_DM11,in);
	}

	/**
	 * constructor from Byte[]
	 * @param array - the Byte Object array 
	 * @return 
	 */
	public DM11Data(Byte[] array) {
		super(TAG_DM11,array);
	}

	 /**
	  * constructor from Hex String as from toString()
	  * @param ins - 32 byte userdata as read from the tag, an optional "0x" prefix will be ignored
	  */
	public DM11Data(String ins){
		super(TAG_DM11,ins);
	}
	
	/**
	 * provide an initialized Tag
	 * - single Medium
	 * - County and ISIL initialized with default values (hard coded)
	 */
	public DM11Data(){
//		shadowdata=userdata32.clone(); // keep empty shadow in order to mark all blocks tainted
		addUserData(new byte[32]);
		keepshadowdata();
		setCountry(myCountry);
		setISIL(myISIL);		
	}
	
	/**
	 * TODO: this still fails - wrong seed? other system than danish data model?
	 * @return - the newly computed CRC
	 */
	protected int DMCRC() {
		return new CRC().tagCRC(userdata32, 14);
	}

	/**
	 * DM11Data just checks that only decimals will be encoded
	 * @return true if only decimals found
	 */
	public boolean isvalid() {
		if(Barcode().matches("[0-9]+")
			&& tailMatch()
		)
			return true;
		return false;
	}

	protected boolean tailMatch(){
		return (
				(((byte) 0xf5) == (byte)userdata32[30])
				&& (( 0x55) == (byte)userdata32[31]));
	}

	/**
	 * @return the CRC as int, added up LSB first, MSB last
	 */
	public int getCRC(){
		int res=userdata32[15]&0xff;
		res<<=8;
		res+=userdata32[14]&0xff;
		return res;
	}
	

	/**
	 * @param i - byte position within userdata32
	 * @return the High Nibble from the byte at the position
	 */
	private byte Hnibble(int i) {
		return (byte) ((userdata32[i] & 0xf0) >> 4);
	}
	
	protected char hexCharAt(String in, int pos) {
			
		return (char)(Integer.parseInt(in.substring(pos, pos+1),16));
	}
	
	/**
	 * set the High Nibble from the byte at the position
	 * @param v - the value (cut as 0x0f) to be set
	 * @param i - byte position within userdata32
	 */
	private void setHnibble(char v, int i) {
		v = (char) ((v & 0x0f) << 4);
		userdata32[i] = (char) ((userdata32[i] & 0x0f) | v); 
	}
	
	private void setHLnibble(char v, int i) {
		v = (char) ((v & 0x03) << 4);
		userdata32[i] = (char) ((userdata32[i] & 0xcf) | v); 
	}
	
	private void setHHnibble(char v, int i) {
		v = (char) ((v & 0x03) << 6);
		userdata32[i] = (char) ((userdata32[i] & 0x3f) | v); 
	}
	
	/**
	 * @param i - byte position within userdata32
	 * @return the Low Nibble from the byte at the position
	 */
	private byte Lnibble(int i) {
		return (byte) (userdata32[i] & 0x0f);
	}

	/**
	 * set the Low Nibble from the byte at the position
	 * @param v - the value (cut as 0x0f) to be set
	 * @param i - byte position within userdata32
	 */
	private void setLnibble(char v, int i) {
		v = (char) ((v & 0x0f));
		userdata32[i] = (char) ((userdata32[i] & 0xf0) | v); 		
	}
	
	private void setLLnibble(char v, int i) {
		v = (char) ((v & 0x03));
		userdata32[i] = (char) ((userdata32[i] & 0xfc) | v); 		
	}
	
	private void setLHnibble(char v, int i) {
		v = (char) ((v & 0x03) << 2);
		userdata32[i] = (char) ((userdata32[i] & 0xf3) | v); 		
	}
	

	/**
	 * combine low half-nibble from byte h with high half-nibble of byte l 8-/
	 * @param h
	 * @param l
	 */
	private byte HL2num(byte h, byte l) {
		byte H=(byte)((h & 3) << 2);
	    byte L=(byte)((l & 12) >> 2);
	    return (byte)(H | L);
	}
	
	
	/**
	 * extract the barcode to a string
	 * @return
	 */
	public String Barcode(){
		String res=new String("........");
		char[] build = res.toCharArray();
		
		build[0]=Integer.toHexString(this.HL2num(this.Lnibble(2),this.Hnibble(1))).charAt(0);
		build[1]=Integer.toHexString(this.Hnibble(2)).charAt(0);
		build[2]=Integer.toHexString(this.HL2num(this.Hnibble(3),this.Lnibble(3))).charAt(0);
		build[3]=Integer.toHexString(this.Lnibble(4)).charAt(0);
		build[4]=Integer.toHexString(this.HL2num(this.Lnibble(5),this.Hnibble(4))).charAt(0);
		build[5]=Integer.toHexString(this.Hnibble(5)).charAt(0);
		build[6]=Integer.toHexString(this.HL2num(this.Hnibble(6),this.Lnibble(6))).charAt(0);
		build[7]=Integer.toHexString(this.Lnibble(7)).charAt(0);
		
		return new String(build);
	}
	
	/**
	 * fill in the Barcode
	 * this is really ugly..
	 * @param bc
	 */
	public void setBarcode(String bc){
		if(!bc.matches("[0-9a-f]+")) return;
		setLLnibble((char)((hexCharAt(bc,0) & 0xc) >> 2), 2);
		setHHnibble(hexCharAt(bc,0), 1);

		setHnibble(hexCharAt(bc,1), 2);

		setLHnibble(hexCharAt(bc,2), 3);
		setHLnibble((char)((hexCharAt(bc,2) & 0xc) >> 2), 3);		

		setLnibble(hexCharAt(bc,3), 4);

		setLLnibble((char)((hexCharAt(bc,4) & 0xc) >> 2), 5);
		setHHnibble(hexCharAt(bc,4), 4);

		setHnibble(hexCharAt(bc,5), 5);

		setLHnibble(hexCharAt(bc,6), 6);
		setHLnibble((char)((hexCharAt(bc,6) & 0xc) >> 2), 6);		
		
		setLnibble(hexCharAt(bc,7), 7);
	}

	public int updateCRC() {
		// TODO Auto-generated method stub
		return 0;
	}


}
