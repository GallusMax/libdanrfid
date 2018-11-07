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
		assertNotNull(data.toString());
	}

	@Test
	public void testCompareCRC() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		data.toString();
		assertEquals((int)0x3e51, data.getCRC());
	}

	@Test
	public void testGetCRC() {
		assertNotNull(new DDMData().getCRC());
	}

	@Test
	public void testcompareCRC() {
		DDMData data = new DDMData();
		assertNotNull(data.toString()); //sets CRC
		assertTrue(data.compareCRC());
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
