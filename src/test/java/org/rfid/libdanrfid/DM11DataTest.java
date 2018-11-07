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
		DM11Data data = new DM11Data("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		String barcode = data.Barcode();
		assertEquals("11223344",barcode);
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
