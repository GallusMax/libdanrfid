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
		super(in);
	}

	/**
	 * constructor from Byte[]
	 * @param array - the Byte Object array 
	 * @return 
	 */
	public DM11Data(Byte[] array) {
		super(array);
	}

	 /**
	  * constructor from Hex String as from toString()
	  * @param ins - 32 byte userdata as read from the tag, an optional "0x" prefix will be ignored
	  */
	public DM11Data(String ins){
		super(ins);
	}
	
	/**
	 * provide an initialized Tag
	 * - single Medium
	 * - County and ISIL initialized with default values (hard coded)
	 */
	public DM11Data(){
		shadowdata=userdata32.clone(); // keep empty shadow in order to mark all blocks tainted
		setCountry(myCountry);
		setISIL(myISIL);		
	}
	
	/**
	 * test on CRC fields
	 * TODO: this still fails - seed?
	 * @return
	 */
	protected int DMCRC() {
		return new CRC().tagCRC(userdata32, 14);
/*		char[] work=userdata32.clone();
		work[14]=0;
		work[15]=0;
		return new CRC().computeCRC(work);
*/
	}

	/**
	 * read the CRC as int, added up LSB first, MSB last
	 * @return
	 */
	public int getCRC(){
		int res=userdata32[15]&0xff;
		res<<=8;
		res+=userdata32[14]&0xff;
		return res;
	}
	

	/**
	 * get high nibble from byte 
	 * @param i
	 */
	private byte Hnibble(int i) {
		return (byte) ((userdata32[i] & 0xf0) >> 4);
	}
	
	/**
	 * get low nibble from byte 
	 * @param i
	 */
	private byte Lnibble(int i) {
		return (byte) (userdata32[i] & 0x0f);
	}

	/**
	 * combine low half-nibble from byte h with high half-nibble of byte l 8-/
	 * @param h
	 * @param l
	 * @return
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
		
		build[0]=String.format("%1d",this.HL2num(this.Lnibble(2),this.Hnibble(1))).charAt(0);
		build[1]=String.format("%1d",Hnibble(2)).charAt(0);
		build[2]=String.format("%1d",this.HL2num(this.Hnibble(3),this.Lnibble(3))).charAt(0);
		build[3]=String.format("%1d",Lnibble(4)).charAt(0);
		build[4]=String.format("%1d",this.HL2num(this.Lnibble(5),this.Hnibble(4))).charAt(0);
		build[5]=String.format("%1d",Hnibble(5)).charAt(0);
		build[6]=String.format("%1d",this.HL2num(this.Hnibble(6),this.Lnibble(6))).charAt(0);
		build[7]=String.format("%1d",Lnibble(7)).charAt(0);
		
		return new String(build);
	}
	
	/**
	 * fill in the Barcode
	 * @param bc
	 */
	public void setBarcode(String bc){
		setStringAt(bc, 3, 16);
	}
	
}
