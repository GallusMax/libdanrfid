package org.rfid.libdanrfid;

public class DDMTag extends DDMData {

	public static final byte AFI_ON=(byte)0x07;
	public static final byte AFI_OFF=(byte)0xC2;

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
	 * set the DDMData part with the given byte[] in 
	 * @param in byte[] containing prepared UserData according DanishDataModel
	 */
	public void addUserData(byte[] in){
		initdata(in);
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

	/**
	 * 
	 * @return formatted String showing the barcode and optional an item count 
	 * @author Ulrich Hahn 
	 * @since 0.4x (?)
	 */	
	public String toString(){
		StringBuilder res=new StringBuilder(Barcode());
		if(1<getofParts())
			res.append(" "+getPartNum()+"/"+getofParts());
		if(isTainted)
			res.append(" needs write");
		return (res.toString());
	}
	
	public DDMTag(byte[] in) {
		super(in);
		// Auto-generated constructor stub
	}

	public DDMTag(Byte[] array) {
		super(array);
		// Auto-generated constructor stub
	}

	public DDMTag(String ins) {
		super(ins);
		// Auto-generated constructor stub
	}

	public DDMTag() {
//		super();
		// Auto-generated constructor stub
	}

	/**
	 * formatted line output for a Panel view. 
	 * In the meantime it just overrides DDMData:toString()
	 * @return formatted String showing the barcode and optional an item count 
	 * @author Ulrich Hahn 
	 * @since 0.4x (?)
	 */
	public String toDisplayLine() {
		return this.toString();
	}
	
}
