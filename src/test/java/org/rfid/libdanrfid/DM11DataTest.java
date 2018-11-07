package org.rfid.libdanrfid;

import static org.junit.Assert.*;

import org.junit.Test;

public class DM11DataTest {

	@Test
	public void testDM11DataString() {
		assertNotNull(new DM11Data("11010131313232333334340000000000000000513e4445373035000000000000").toString()); // just simple
	}

	@Test
	public void testToString() {
		DM11Data data = new DM11Data();
		String readData = data.toString();
		DM11Data dataFromString = new DM11Data(readData);
		assertEquals(readData, dataFromString.toString());
	}

	@Test
	public void testIsValid() {
		DM11Data data = new DM11Data("11010131313232333334340000000000000000"); // too short, no CRC
		assertTrue(data.isvalid());
		assertNotNull(data.toString());
		assertTrue(data.isvalid());
	}
	
	@Test
	public void testBarcode() {
		assertEquals("12345678",new DM11Data("0B41244D54655D18000000000000BB4CA484FF4F42504401500400000000F555").Barcode());
		assertEquals("10000000",new DM11Data("0B410441100441100000000000000ACDA484FF4F42504401500400000000F555").Barcode());
		assertEquals("12345670",new DM11Data("0B41244D54655D10000000000000E148A484FF4F42504401500400000000F555").Barcode());
		assertEquals("12345001",new DM11Data("0B41244D54054111000000000000DAE9A484FF4F42504401500400000000F555").Barcode());
		assertEquals("11111111",new DM11Data("0B41144551144511000000000000EFD6A484FF4F42504401500400000000F555").Barcode());
		assertEquals("22222222",new DM11Data("0B812449922449120000000000001230A484FF4F42504401500400000000F555").Barcode());
		assertEquals("33333333",new DM11Data("0BC1344DD3344D13000000000000448BA484FF4F42504401500400000000F555").Barcode());
		assertEquals("44444444",new DM11Data("0B014551144551140000000000007CCBA484FF4F42504401500400000000F555").Barcode());
	}

	@Test
	public void testBarcodematch() {
		DM11Data data = new DM11Data("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		assertTrue(data.barcodematch("11223.+"));
		assertFalse(data.barcodematch("abc.+"));
	}

	@Test
	public void testSetBarcode() {
		DM11Data data = new DM11Data();
		data.setBarcode("1g2h3j");
		assertEquals("1g2h3j", data.Barcode());
	}

}
