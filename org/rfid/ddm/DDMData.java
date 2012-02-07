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

package org.rfid.ddm;

/**
 * A representation of the mandatory fields within the Danish Data Format
 * found on the RFID Tags of some Libraries
 * 
 * @author uhahn
 *
 */
public class DDMData {
	/*
	 * These presets are used when a blank record is created
	 * customize here - ugly, too!
	 */
	public final String myCountry="DE";
	public final String myISIL="0705";
	
	/* one byte combines Version and media usage 
	 * this is as ugly a these constants
	 */
	public final char V1NEU=16; 
	public final char V1AUSLEIHBAR=17; 
	public final char V1PRAESENZ=18; 
	public final char V1GELOESCHT=23; 
	public final char V1KUNDE=24; 

	/*
	 * some like it forward, some reverse
	 * still looking for a performant way of handling this
	 * a cure would be a reference implementation 
	 * 
	 */
	protected char[] forward32 = new char[32]; // represents the tag user fields
//	protected char[] reverse32 = new char[32]; // reversed block order
	protected char[] userdata32=forward32;
	
	public DDMData(byte[] in){
		for (int i = 0; i < 32; i++) { // dont trust in length ;-) 
			if(i<in.length) // any case..
				userdata32[i]=(char)in[i];
			else
				userdata32[i]=(char)0; // init all
		}
	}

	/**
	 * provide an initialized Tag
	 */
	public DDMData(){
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
	 * change the order of each block 
	 * FKI writes the blocks from 3 to 0, Biblioteca writes 0 to 3
	 */
	protected void reverseblocks(){
		int blocksize=4;
		int numblocks=8;
		for (int k = 0; k < numblocks; k++) { // all blocks
				reverse(userdata32,k*blocksize,blocksize); // swap string positions
		}
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
	}
	
	/**
	 * change the usage 
	 * @param usage
	 * n.b.: the version is included here
	 */
	public void setUsage(char usage){
		userdata32[0]=usage;
	}

	/**
	 * 
	 * @return the usage nibble
	 */
	public int getUsage(){
		return userdata32[0]&0x0f;
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
	
	
}
