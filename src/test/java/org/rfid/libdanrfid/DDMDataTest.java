package org.rfid.libdanrfid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DDMDataTest {

	@Test
	void testDDMDataString() {
		assertNotNull(new DDMData("11010131313232333334340000000000000000513e4445373035000000000000").toString()); // just simple
	}

	@Test
	void testToString() {
		DDMData data = new DDMData();
		assertNotNull(data.toString());
	}

	@Test
	void testCompareCRC() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		data.toString();
		assertEquals((int)0x3e51, data.getCRC());
	}

	@Test
	void testGetCRC() {
		assertNotNull(new DDMData().getCRC());
	}

	@Test
	void testcompareCRC() {
		DDMData data = new DDMData();
		assertNotNull(data.toString()); //sets CRC
		assertTrue(data.compareCRC());
	}
	
	@Test
	void testIsValid() {
		DDMData data = new DDMData("11010131313232333334340000000000000000"); // too short, no CRC
		assertFalse(data.isvalid());
		assertNotNull(data.toString());
		assertTrue(data.isvalid());
	}
	
	@Test
	void testBarcode() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		String barcode = data.Barcode();
		assertEquals("11223344",barcode);
	}

	@Test
	void testBarcodematch() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		assertTrue(data.barcodematch("11223.+"));
		assertFalse(data.barcodematch("abc.+"));
	}

	@Test
	void testSetBarcode() {
		DDMData data = new DDMData();
		data.setBarcode("1g2h3j");
		assertEquals("1g2h3j", data.Barcode());
	}

	@Test
	void testCountry() {
		DDMData data = new DDMData();
		assertEquals(data.myCountry, data.Country());
	}

	@Test
	void testSetCountry() {
		DDMData data = new DDMData();
		data.setCountry("XY");
		assertEquals("XY", data.Country());
	}

	@Test
	void testISIL() {
		DDMData data = new DDMData();
		assertEquals(data.myISIL, data.ISIL());
	}

	@Test
	void testSetISIL() {
		DDMData data = new DDMData();
		data.setISIL("ISIL");
		assertEquals("ISIL", data.ISIL());
	}

}
