/**
 * 
 */
package org.rfid.libdanrfid;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author uhahn
 *
 */
public class CRCTest {

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#CRC()}.
	 */
	@Test
	public void testCRC() {
		assertEquals("RFID tag data model", CRC.referenceString);
	}

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#CRC(char[])}.
	 */
	@Test
	public void testCRCCharArray() {
	}

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#CRC(java.lang.String)}.
	 */
	@Test
	public void testCRCString() {
		assertEquals(CRC.referenceCRC, new CRC(CRC.referenceString).gethex());
	}

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#getint()}.
	 */
	@Test
	public void testGetint() {
		assertEquals(0x1aee, new CRC(CRC.referenceString).getint());
	}

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#gethex()}.
	 */
	@Test
	public void testGethex() {
		CRC test = new CRC();
		test.crc_sum=0xabcd;
		assertEquals("abcd", test.gethex());
	}

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#DDCRC(char[])}.
	 */
	@Test
	public void testDDCRC() {
		CRC test = new CRC();
		DM11Data dm11test = new DM11Data("11010131313232333334340000000000000000513e4445373035000000000000");
		assertEquals(0x3e51, test.DDCRC(dm11test.userdata32));
	}

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#tagCRC(char[], int)}.
	 */
	@Test
	public void testTagCRC() {
		DM11Data dm11test = new DM11Data("11010131313232333334340000000000000000513e4445373035000000000000");
		assertEquals(0x3e51, new CRC().tagCRC(dm11test.userdata32,19));
	}

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#computeCRC(char[])}.
	 */
	@Test
	public void testComputeCRC() {
		assertEquals(55918, new CRC().computeCRC(new String("11010131313232333334340000000000000000").toCharArray()));
	}

	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#update_crc(int)}.
	 */
	@Test
	public void testUpdate_crc() {
//		fail("Not yet implemented");
	}

}
