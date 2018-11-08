package org.rfid.libdanrfid;

import static org.junit.Assert.*;

import org.junit.Test;

public class DDMDataTest {

	@Test
	public void testDDMDataString() {
		assertNotNull(new DDMData("11010131313232333334340000000000000000513e4445373035000000000000").toString()); // just simple
	}

	@Test
	public void testToString() {
		DDMData data = new DDMData();
		String readData = data.toString();
		DDMData dataFromString = new DDMData(readData);
		assertEquals(readData, dataFromString.toString());
	}

	@Test
	public void testGetCRC() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		data.toString();
		assertEquals((int)0x3e51, data.getCRC());
	}

	@Test
	public void testcomputeCRC() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000");
//		assertNotNull(data.toString()); //sets CRC
		assertTrue(data.compareCRC());
		assertEquals(data.getCRC(),new CRC().tagCRC(data.userdata32,19));
		
	}
	
	@Test
	public void testIsValid() {
		DDMData data = new DDMData("11010131313232333334340000000000000000"); // too short, no CRC
		assertFalse(data.isvalid());
		assertNotNull(data.toString());
		assertTrue(data.isvalid());
	}
	
	@Test
	public void testBarcode() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		String barcode = data.Barcode();
		assertEquals("11223344",barcode);
	}

	@Test
	public void testBarcodematch() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		assertTrue(data.barcodematch("11223.+"));
		assertFalse(data.barcodematch("abc.+"));
	}

	@Test
	public void testSetBarcode() {
		DDMData data = new DDMData();
		data.setBarcode("1g2h3j");
		assertEquals("1g2h3j", data.Barcode());
	}

	@Test
	public void testCountry() {
		DDMData data = new DDMData();
		assertEquals(data.myCountry, data.Country());
	}

	@Test
	public void testSetCountry() {
		DDMData data = new DDMData();
		data.setCountry("XY");
		assertEquals("XY", data.Country());
	}

	@Test
	public void testISIL() {
		DDMData data = new DDMData();
		assertEquals(data.myISIL, data.ISIL());
	}

	@Test
	public void testSetISIL() {
		DDMData data = new DDMData();
		data.setISIL("ISIL");
		assertEquals("ISIL", data.ISIL());
	}

}
