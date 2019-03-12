/**
 * This file is part of libdanrfid, a toolkit for RFID Library Tags.
 * Copyright (C) 2013 Ulrich Hahn
 * 
 * libdanrfid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * libdanrfid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with libdanrfid. If not, see <https://www.gnu.org/licenses/>.
 */

package org.rfid.libdanrfid;

/**
 * @author uhahn
 *
 */
public class DDMTag extends LibTag {

	/**
	 * @param userdata
	 */
	public DDMTag(byte[] userdata) {
		super(userdata);
		tagType=TAG_DDM;
//		addUserData(userdata);
	}

	/**
	 * @param hexstring
	 */
	public DDMTag(String hexstring) {
		super(hexstring);
		tagType=TAG_DDM;
	}

	/**
	 * instantiate a dummy DDM tag
	 */
	public DDMTag() {
		super(TAG_DDM);
		tagType=TAG_DDM;
	}
	
	/**
	 * test purposes: preset with userdata
	 * @param in - the user data hex string to be set
	 */
	public void addUserData(byte[] in) {
		ddmInstance.addUserData(in);
	}

	/**
	 * export the userdata as hex (DDMTag special)
	 */
	public String toString() {
		return ddmInstance.toString();
	}
	
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
		
		DDMTag td=new DDMTag();
		td.setBarcode(args[0]);
		System.out.format("Barcode %s\n", td.Barcode());
		System.out.format("toString %s\n", td.toString());
		System.out.format("toDisplayLine %s\n", td.toDisplayLine());
//		System.out.format("userdata32 %s\n", Util.toHex(td.userdata32));
		
	}
	
	public static void usage(){
		System.out.format("usage: $0 <barcode>\n");
		System.out.format("will return the 32 byte userdata of a RFID Tag\n");
	}

	/**
	 * formatted line output for a Panel view. 
	 * @return formatted String showing the barcode and optional an item count 
	 * @author Ulrich Hahn 
	 * @since 0.4x (?)
	 */
	public String toDisplayLine() {
		StringBuilder res=new StringBuilder(Barcode());
		if(1<getofParts())
			res.append(" "+getPartNum()+"/"+getofParts());
		if(isTainted)
			res.append(" needs write");
		return (res.toString());
	}


}
