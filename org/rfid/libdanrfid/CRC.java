/*
$Id: $
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

public class CRC {
	final int crc_poly=0x1021;
	int crc_sum=0xffff;
	
	static final String referenceString="RFID tag data model";
	static final String referenceCRC="1aee";
	
	/**
	 * command line CRC generation
	 * @param args
	 */
	public static void main(String[] args) {
		String teststring;
		if(0<args.length)
			teststring=args[0];
		else{
			System.err.println("when called with no arguments the reference string is used");
			System.err.println("which should produce a CRC value of 1aee");
			teststring=referenceString;
		}
//			System.out.format("read %s\n", args[0]);

		final int n=teststring.length();

//		System.out.format("crc von %s: %s",teststring,new CRC(teststring).gethex());
//		computeCRC(ca);
		System.out.println(new CRC(teststring).gethex());	

	}
	
	public CRC(){		
	}
	
	public CRC(char[] ba) {
		computeCRC(ba);
	}

	public CRC(String s) {
		computeCRC(s.toCharArray());
	}

	public int getint(){
		return crc_sum;
	}
	
	public String gethex(){
		return String.format("%h", crc_sum);
	}

	/**
	 * return the CRC sum of the (at most)32 bytes of a RFID tag keeping in mind 
	 * to fill up with nullbytes to 34 
	 * the updating of byte pos 19 and 20 is left to the TagData instance
	 * @param tag
	 * @return
	 */
	public int DDCRC(char[] tag){
		crc_sum=0xffff;
		for (int i = 0; i < tag.length; i++) {
			if((19==i)||(20==i)) continue; //skip the position of the CRC thus ignoring if there is one already
//System.out.format("%2h",tag[i]);
			update_crc(tag[i]);
//System.out.format("%2h - %4x\n",(char)tag[i],crc_sum );
		}
		for (int i = tag.length; i < 34; i++) {
			update_crc(0); // include two more null bytes to achieve the full length if ISIL code
		}
		return crc_sum;
	}
	
	private int computeCRC(char[] s){
		crc_sum=0xffff;

		for (char b : s) {
			update_crc(b);
		}
		return crc_sum;
	}
	
	protected void update_crc(int c)
	{
	int i;
	boolean xor_flag;
	c&=0xff;
	c<<=8;
	for(i=0; i<8; i++)
	{
	xor_flag=((crc_sum ^ c) & 0x8000)!=0;
	crc_sum = crc_sum << 1;
	if (xor_flag) crc_sum = crc_sum ^ crc_poly;
	c = c << 1;
	}
	crc_sum&=0xffff;
	}

}
