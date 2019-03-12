/**
 * 
 */
package org.rfid.libdanrfid;

/**
 * @author uhahn
 *
 */
public class DDMTag extends LibTag {

	/**
	 * @param userdata
	 */
	public DDMTag(byte[] userdata) {
		super(userdata);
		super.tagType=TAG_DDM;
	}

	/**
	 * @param hexstring
	 */
	public DDMTag(String hexstring) {
		super(hexstring);
		super.tagType=TAG_DDM;
	}

	/**
	 * 
	 */
	public DDMTag() {
		super.tagType=TAG_DDM;
	}
	
	/**
	 * @param args
	 * usage: $0 <barcode> [more args?]
	 * prints out the 32 byte Tag user data 
	 * complete with CRC according Daenisches Datenmodell
	 */
	public static void main(String[] args) {

		if(0==args.length){
			usage();
			return;
		}
		
		DDMTag td=new DDMTag();
		td.setBarcode(args[0]);
		System.out.format("Barcode %s\n", td.Barcode());
		System.out.format("toString %s\n", td.toString());
		System.out.format("toDisplayLine %s\n", td.toDisplayLine());
//		System.out.format("userdata32 %s\n", Util.toHex(td.userdata32));
		
	}
	
	public static void usage(){
		System.out.format("usage: $0 <barcode>\n");
		System.out.format("will return the 32 byte userdata of a RFID Tag\n");
	}


}
