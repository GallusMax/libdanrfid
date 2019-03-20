package org.rfid.libdanrfid;

public class LibTag extends TagData {

	public static final byte AFI_ON=0x07;
	public static final byte AFI_OFF=0x21; // TODO make AFI values configurable in java properties?
	private static final String SysDummyHex="0355443322110004E00007";
	private static final String SysDummyUnlockHex="0355443322110004E00021";

	private byte afi=0;
	public boolean isTainted=true; // we have not read all - or have made changes yet unwritten
	public byte[] sysinfo;
	public boolean afiTainted;  // we just changed tha AFI value and have not written to tag
	
	public byte getAFI(){
		return afi;
	}
	
	/**
	 * 
	 * @param AFI the new AFI value. Use (static) AFI_ON or AFI_OFF!
	 */
	public void setAFI(byte AFI){
		this.afi=AFI;
//		isTainted=true;
		afiTainted=true;
	}

	/**
	 * 
	 * @return true, if currently AFI-locked 
	 */
	public boolean isAFIsecured(){
		return (AFI_ON==afi);
	}
	
	/**
	 * 
	 * @return 8 bytes UID from Tag's sysinfo as Hex in uppercase. e.g. "E004010031323334"
	 */
	public String getUID(){
		return Util.reverseHex(Util.toHex(sysinfo).substring(2,18).toUpperCase()); 
	}
	
	/**
	 * set the system part of the DDMTag from the given hex string s
	 * @param s hex representation of system data
	 * @return a byte[] containing the system data 
	 */
	public byte[] addSystemInformation(String s){
		//TODO - skip first 0x00 byte from Android NfcV
		if(s.startsWith("0x")) // rip off the leading 0x byte from hex String
			s=s.substring(2);
		if(s.startsWith("000")) // rip off the leading 00 byte from NfcV..
			s=s.substring(2);
		if((16==s.length() && (s.toUpperCase().startsWith("E004")))){ // we have just an UID here
			return null;
		}else
			return addSystemInformation(Util.hexStringToByteArray(s));
		// TODO what about tainted?!
	}

	/**
	 * set the system part of the DDMTag from tag data
	 * @param read byte[] as read from tag
	 * @return a byte[] containing the system data 
	 */
	public byte[] addSystemInformation(byte[] read){
		sysinfo=read;
//		if(0==read[0]){// no error byte from Android NfcV
//			String s=new String(read);
			//s.substring(2, 9).compareTo(Id.toString())  // the same?
			char flags=(char)read[0]; //s.charAt(1);
			int pos=9; // start after flags, Infoflags and Id 
			if(0<(flags&0x1)){ // DSFID valid
				pos++; // already implemented 
			}
			if(0<(flags&0x2)){ // AFI valid
				afi=(byte)read[pos++];//s.charAt(pos++);
			}
			if(0<(flags&0x4)){ // memsize valid
				nBlocks=(byte)(read[pos++]+1);//(s.charAt(pos++)+1);
				blocksize=(byte)(read[pos++]+1); //((s.charAt(pos++)&0x1f)+1);
			}
		
		isTainted=false; // remember: we have read it and found it valid
		return read;
//		}
	}

	public LibTag(byte[] userdata) {
		super(userdata);
	}

	public LibTag(String hexstring) {
		super(hexstring);
	}

	/**
	 * this constructor currently creates a pure dummy tag, 
	 * locked and with an obviously bogus UID
	 */
	public LibTag() {
		super();
		addSystemInformation(Util.hexStringToByteArray(SysDummyHex));
	}

	public LibTag(int currentType) {
		tagType=currentType;
		switch(tagType) {
			case TAG_DDM:	
				addUserData(Util.hexStringToByteArray(new DDMData().toString()));
				break;
			case TAG_DM11:
				addUserData(Util.hexStringToByteArray(new DM11Data().toString()));
				break;
		}
	}

	
	/**
	 * formatted line output for a Panel view. to be overridden
	 * @return formatted String showing the barcode and optional an item count 
	 * @author Ulrich Hahn 
	 * @since 0.4x (?)
	 */
	public String toDisplayLine() {
		return "should be overridden";
	}

	
}
