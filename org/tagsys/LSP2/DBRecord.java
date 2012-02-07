package org.tagsys.LSP2;

import java.util.Date;

import org.rfid.ddm.DDMData;

/**
 * stores Tag Data read from a LSP2 
 * together with a Timestamp and Tag Type  
 * @author uhahn
 *
 */
public class DBRecord extends DDMData {

	char[] read = new char[40];
	final int of = 8; // 6 bytes Timestamp + 2 bytes TagType??
	
	public DBRecord(byte[] in) {
		int n=(in.length<40?in.length:40);
		for (int i = 0; i < n; i++) {
			read[i]=(char)in[i];
		}
		filltag();
		for (char d : userdata32) {
			System.out.format("%02x", (byte)d);			
		}

	}
	
	/**
	 * create a Date out of the first six bytes of the record
	 * @return
	 */
	public  Date Date(){
		char[] da=read;
		Date d=new Date(da[2]+100,da[0]-1,da[1],da[3],da[4],da[5]);
		return d;
	}

	/**
	 * reads the blocks into the userdata32 array
	 */
	private void filltag(){
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				userdata32[i*4+j]=read[of+ (7-i)*4 +j];
			}
		}
	}

}
