package org.rfid.libdanrfid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DDMTagTest {

	// test values without blocksize/memsize info!
	private static final String SysDummyHex="0355443322110004E00007";
	private static final String SysDummyUnlockHex="0355443322110004E00021";

	@Test
	public void testDDMTagString() {
		assertNotNull(new DDMTag("11010131313232333334340000000000000000513e4445373035000000000000").toString()); // just simple
	}

	@Test
	public void testToString() {
		DDMTag data = new DDMTag("11010131313232333334340000000000000000513e4445373035000000000000");
		assertEquals("11223344 needs write", data.toString());
	}

	@Test
	public void testCompareCRC() {
		DDMData data = new DDMData("11010131313232333334340000000000000000513e4445373035000000000000"); // just simple
		data.toString();
		assertEquals((int)0x3e51, data.getCRC());
	}

	@Test
	public void testsetGetAFI() {
		DDMTag data = new DDMTag();
		data.setAFI((byte)0x42);
		assertEquals((byte)0x42,data.getAFI());
		assertTrue(data.afiTainted);
	}
	
	@Test
	public void testisAFIsecured() {
		DDMTag data = new DDMTag("11010131313232333334340000000000000000"); // too short, no CRC
		data.setAFI(DDMTag.AFI_ON);
		assertTrue(data.isAFIsecured());
		assertTrue(data.afiTainted);
	}

	@Test
	public void testaddSystemInfo() {
		DDMTag data = new DDMTag("11010131313232333334340000000000000000513e4445373035000000000000");
		data.addSystemInformation(Util.hexStringToByteArray(SysDummyHex));
		assertEquals("E004001122334455",data.getUID());
		assertTrue(data.isAFIsecured());
		assertFalse(data.isTainted);
	}

	@Test
	public void testaddSystemInfo0x() {
		DDMTag data = new DDMTag("11010131313232333334340000000000000000513e4445373035000000000000");
		data.addSystemInformation("0x"+SysDummyHex);
		assertEquals("E004001122334455",data.getUID());
		assertTrue(data.isAFIsecured());
		assertFalse(data.isTainted);
	}

	@Test
	public void testgetUID() {
		DDMTag data = new DDMTag("11010131313232333334340000000000000000513e4445373035000000000000");
		data.addSystemInformation(SysDummyHex);
		assertEquals("E004001122334455",data.getUID());
		assertTrue(data.isAFIsecured());
		data.addSystemInformation(SysDummyUnlockHex);
		assertFalse(data.isAFIsecured());
	}

	@Test
	public void testaddUserdata() {
		DDMTag data = new DDMTag();
		data.addUserData(Util.hexStringToByteArray("11010131313232333334340000000000000000513e4445373035000000000000"));
		assertEquals("11223344",data.Barcode());
	}

}
