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
		data = new DM11Data("11010131313232333334340000000000000000513e4445373035000000000000");
		readData = data.toString();
		dataFromString = new DM11Data(readData);
		assertEquals(readData, dataFromString.toString());
	}

	@Test
	public void testIsValid() {
		DM11Data data = new DM11Data("0B41244D54655D18000000000000BB4CA484FF4F42504401500400000000F555");
		assertTrue(data.isvalid());
		assertNotNull(data.toString());
		assertTrue(data.isvalid());
		data = new DM11Data("11010131313232333334340000000000000000513e4445373035000000000000");
		assertFalse(data.isvalid());
	}
	
	@Test
	public void testGetCRC() {
		assertEquals(0x4cbb,new DM11Data("0B41244D54655D18000000000000BB4CA484FF4F42504401500400000000F555").getCRC());
		assertEquals(0xcd0a,new DM11Data("0B410441100441100000000000000ACDA484FF4F42504401500400000000F555").getCRC());
		assertEquals(18657,new DM11Data("0B41244D54655D10000000000000E148A484FF4F42504401500400000000F555").getCRC());
	}
	
	@Test 
	public void testDMCRC() {
		DM11Data testDm11=new DM11Data("0B41244D54655D18000000000000BB4CA484FF4F42504401500400000000F555");
//		assertEquals(testDm11.getCRC(), new CRC().tagCRC(testDm11.userdata32,14)); // same as DMCRC()
//		assertEquals(testDm11.getCRC(), testDm11.DMCRC());
		//TODO - CRC method unknown
	}
	
	@Test
	public void testBarcode() {
		assertEquals("12345678",new DM11Data("0B41244D54655D18000000000000BB4CA484FF4F42504401500400000000F555").Barcode());
		assertEquals("10000000",new DM11Data("0B410441100441100000000000000ACDA484FF4F42504401500400000000F555").Barcode());
		assertEquals("1f0f0f0f",new DM11Data("0B41f4411ff4411f0000000000000ACDA484FF4F42504401500400000000F555").Barcode());
		assertEquals("12345670",new DM11Data("0B41244D54655D10000000000000E148A484FF4F42504401500400000000F555").Barcode());
		assertEquals("12345001",new DM11Data("0B41244D54054111000000000000DAE9A484FF4F42504401500400000000F555").Barcode());
		assertEquals("11111111",new DM11Data("0B41144551144511000000000000EFD6A484FF4F42504401500400000000F555").Barcode());
		assertEquals("22222222",new DM11Data("0B812449922449120000000000001230A484FF4F42504401500400000000F555").Barcode());
		assertEquals("33333333",new DM11Data("0BC1344DD3344D13000000000000448BA484FF4F42504401500400000000F555").Barcode());
		assertEquals("44444444",new DM11Data("0B014551144551140000000000007CCBA484FF4F42504401500400000000F555").Barcode());
	}

	@Test
	public void testBarcodematch() {
		DM11Data data = new DM11Data("0B41244D54655D10000000000000E148A484FF4F42504401500400000000F555"); // just simple
		assertTrue(data.barcodematch("1234.+"));
		assertFalse(data.barcodematch("abc.+"));
	}

	@Test
	public void testSetBarcode() {
		DM11Data data = new DM11Data();
		String testcode="76543210";
		data.setBarcode(testcode);
		assertEquals(testcode, data.Barcode());
		testcode= new String("11111111");
		data.setBarcode(testcode);
		assertEquals(testcode, data.Barcode());
		testcode= new String("aaaa1d1d");
		data.setBarcode(testcode);
		assertEquals(testcode, data.Barcode());
		testcode= new String("abcd1234");
		data.setBarcode(testcode);
		assertEquals(testcode, data.Barcode());

		TagData dd = new DDMData();
//		assertEquals("test",dd.toString());
		assertEquals(0x9fca,new CRC(0x1021).tagCRC(dd.userdata32,19));

		data = new DM11Data();
		data.setBarcode("12345678");
		assertEquals("12345678",data.Barcode());
		// should be equal - after tweaking the generation + CRC
//		assertEquals(DM11Data.UserDefaultHex,data.toString());
//		assertEquals(0x4cbb,new CRC(0x8005).tagCRC(data.userdata32,14));
//		assertEquals(0x4cbb,new CRC(0x1DCF).tagCRC(data.userdata32,14));
//		assertEquals(DM11Data.UserDefaultHex,data.toString());
	}

	@Test
	public void testTwinUserdata(){
		// created Userdata - strange: same Barcode() result?
		DM11Data data;
		data = new DM11Data("0040200c44611c08000000000000000000000000000000000000000000000000");
		data = new DM11Data("0B41244d54655d18000000000000000000000000000000000000000000000000");
		assertEquals(TagData.TAG_DM11,data.tagType);
		assertEquals("12345678",data.Barcode());

		// sample Userdata
		data = new DM11Data("0B41155DD9945113944100000000FD32A484FF4F42504401500400000000F555");
		assertEquals(TagData.TAG_DM11,data.tagType);
		assertEquals("51793943",data.Barcode());

		data = new DM11Data("0B41054D107655189441000000004B9FA484FF4F42504401500400000000F555");
		assertEquals(TagData.TAG_DM11,data.tagType);
		assertEquals("50308758",data.Barcode());

	}
	
	@Test
	public void testgetVersionUsage() {
		TagData data;
		data = new DM11Data();
		assertEquals(0x0b, data.getVersionByte());
		
	}
			

}
