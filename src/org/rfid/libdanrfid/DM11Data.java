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
		initdata(in);
	}

	/**
	 * constructor from Byte[]
	 * @param array - the Byte Object array 
	 * @return 
	 */
	public DM11Data(Byte[] array) {
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
	public DM11Data(String ins){
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
	public DM11Data(){
		shadowdata=userdata32.clone(); // keep empty shadow in order to mark all blocks tainted
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
	 * get high nibble from byte 
	 * @param i
	 */
	private char nibbleH(int i) {
		
		return (char) ((userdata32[i] & 0xf0) >> 4);
	}
	
	/**
	 * get low nibble from byte 
	 * @param i
	 */
	private char nibbleL(int i) {
		
		return (char) ((userdata32[i] & 0x0f));
	}
	
	/**
	 * extract the barcode to a string
	 * @return
	 */
	public String Barcode(){
		String res=new String("........");
		char[] build = res.toCharArray();

		build[1]=nibbleH(2);
		build[3]=nibbleL(4);
		build[4]=nibbleH(5);
		build[7]=nibbleL(7);
// TODO fiddle ot the split digits		
		return build.toString();
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
	 * does this Itemcode belong to us? 
	 * @param barcodePattern - the pattern to be checked against
	 * @return - true if matches, or null or empty Pattern given
	 */
	public boolean barcodematch(String barcodePattern) {
		if(null==barcodePattern)return true;
		if(barcodePattern.isEmpty()) return true;
		return Barcode().matches(barcodePattern);
	}

	/**
	 * rfresh the CRC at the given position
	 * @return the int value of the new CRC 
	 */
	public int updateCRC() {
		int crc_sum=new CRC().DDCRC(userdata32);
	
		// replace existing CRC in given array
		if(userdata32.length<21)return crc_sum; // dont step beyond - shouldnt happen
		userdata32[19]=(char)(crc_sum & 0xff); // lsb first
		int msb=crc_sum ;
		msb>>=8;
		userdata32[20]=(char)(msb & 0xff);
		return crc_sum;
	}
	

	
}
