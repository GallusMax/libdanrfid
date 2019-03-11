package org.rfid.libdanrfid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DDMTagTest {

	private static final String UserDummyHex="11010131313232333334340000000000000000513e4445373035000000000000";
	private static final String UserEmptyHex="11010100000000000000000000000000000000ca9f4445373035000000000000";
	
	// test values without blocksize/memsize info!
	private static final String SysDummyHex="0355443322110004E00007";
	private static final String SysDummyUnlockHex="0355443322110004E00021";

	@Test
	public void testDDMTagToString() {
		assertNotNull(new LibTag().toString()); // just simple
		assertEquals(UserEmptyHex, new LibTag().toString());
	}

	@Test
	public void testFromString() {
		LibTag data = new LibTag(UserDummyHex);
		assertEquals(UserDummyHex, data.toString());
	}

	@Test
	public void testFoundCRC() {
		LibTag data = new LibTag("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
//		data.toString();
		assertEquals((int)0x3e51, data.getCRC());
	}

	@Test
	public void testBarcode() {
		String testcode="07050706";
//		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		LibTag data = new LibTag();
		data.setBarcode(testcode);
		String barcode = data.Barcode();
		assertEquals(testcode,barcode);
		LibTag tag = new LibTag("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		String tbarcode = tag.Barcode();
		assertEquals("11223344",tbarcode);
	}

	@Test
	public void testsetGetAFI() {
		LibTag data = new LibTag();
		data.setAFI((byte)0x42);
		assertEquals((byte)0x42,data.getAFI());
		assertTrue(data.afiTainted);
	}
	
	@Test
	public void testisAFIsecured() {
		LibTag data = new LibTag("11010131313232333334340000000000000000"); // too short, no CRC
		data.setAFI(LibTag.AFI_ON);
		assertTrue(data.isAFIsecured());
		assertTrue(data.afiTainted);
	}

	@Test
	public void testaddSystemInfo() {
		LibTag data = new LibTag("11010131313232333334340000000000000000513e4445373035000000000000");
		data.addSystemInformation(Util.hexStringToByteArray(SysDummyHex));
		assertEquals("E004001122334455",data.getUID());
		assertTrue(data.isAFIsecured());
		assertFalse(data.isTainted);
	}

	@Test
	public void testaddSystemInfo0x() {
		LibTag data = new LibTag("11010131313232333334340000000000000000513e4445373035000000000000");
		data.addSystemInformation("0x"+SysDummyHex);
		assertEquals("E004001122334455",data.getUID());
		assertTrue(data.isAFIsecured());
		assertFalse(data.isTainted);
	}

	@Test
	public void testgetUID() {
		LibTag data = new LibTag("11010131313232333334340000000000000000513e4445373035000000000000");
		data.addSystemInformation(SysDummyHex);
		assertEquals("E004001122334455",data.getUID());
		assertTrue(data.isAFIsecured());
		data.addSystemInformation(SysDummyUnlockHex);
		assertFalse(data.isAFIsecured());
	}

	@Test
	public void testaddUserdata() {
		LibTag data = new LibTag();
		data.addUserData(Util.hexStringToByteArray("11010131313232333334340000000000000000513e4445373035000000000000"));
		assertEquals("11223344",data.Barcode());
	}

}
