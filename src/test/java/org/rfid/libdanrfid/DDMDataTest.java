package org.rfid.libdanrfid;

import static org.junit.Assert.*;

import org.junit.Test;

public class DDMDataTest {

	private static final String UserDummyHex="11010131313232333334340000000000000000513e4445373035000000000000";
	private static final String UserEmptyHex="11010100000000000000000000000000000000ca9f4445373035000000000000";
	
	@Test
	public void testDDMDataString() {
		assertEquals(UserDummyHex, new DDMData(UserDummyHex).toString()); // just simple
		assertEquals(UserEmptyHex, new DDMData().toString());
	}

	@Test
	public void testToString() {
		DDMData data = new DDMData();
		String readData = data.toString();
		DDMData dataFromString = new DDMData(readData);
		assertEquals(readData, dataFromString.toString());
		assertTrue(data.compareCRC());
	}

	@Test
	public void testCompareCRC() {
		DDMData data = new DDMData(UserDummyHex); // just simple
		assertTrue(data.compareCRC());
	}
	
	@Test
	public void testGetCRC() {
			DDMData data = new DDMData(UserDummyHex); // just simple
//		assertNotNull(data.toString()); // CRC is OK on test data
		assertEquals((int)0x3e51, data.getCRC());
	}

	@Test
	public void testtagCRC() {
		DDMData data = new DDMData(UserDummyHex);
		assertNotNull(data.toString()); //sets CRC
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
