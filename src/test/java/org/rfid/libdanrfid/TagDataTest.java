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
public class TagDataTest {


	
	
	/**
	 * Test method for {@link org.rfid.libdanrfid.CRC#gethex()}.
	 */
	@Test
	public void initdataTest() {
		TagData data = new TagData("11010131313232333334340000000000000000513e4445373035000000000000");
		assertEquals(TagData.TAG_DDM, data.tagType);
		
		data = new TagData("0B41244D54655D18000000000000BB4CA484FF4F42504401500400000000F555");
		assertEquals(TagData.TAG_DM11, data.tagType);

		data = new TagData("11010131313232333334340000000000000000513e4445373035000000000000");
		assertEquals(TagData.TAG_DDM, data.tagType);
		
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
