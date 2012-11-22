package org.rfid.libdanrfid;

public class Util {

    /**
     * format (lowercase) hex from a byte[]
     * @param in - a byte array 
     * @return - a hex string
     */
	public static String toHex(byte[] in){
        String text="";//String.format("0x");
        if(null==in)return text; // dont puke on null
        for (byte  element : in) {
			text=text.concat(String.format("%02x", element));
		}
        return text;
    }
    
    public static byte[] hexStringToByteArray(String s) {
    	//TODO what if null==s?
    	int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

	/**
	 * reverse a subset of an array, starting at @param offset 
	 * changing at max @param block bytes
	 * CHANGES THE ARRAY IN PLACE
	 */
	public static byte[] reverse(byte[] in, int offset, int block){
		byte bt;
		for (int i = 0; i < block/2; i++) {
			bt=in[offset+i];
			in[offset+i]=in[offset+block-i-1];
			in[offset+block-i-1]=bt;
		}
		return in;
	}

	/**
	 * reverse a subset of an array, starting at @param offset 
	 * changing at max @param block bytes
	 * CHANGES THE ARRAY IN PLACE
	 */
	public char[] reverse(char[] in, int offset, int block){
		char bt;
		for (int i = 0; i < block/2; i++) {
			bt=in[offset+i];
			in[offset+i]=in[offset+block-i-1];
			in[offset+block-i-1]=bt;
		}
		return in;
	}

    
	/**
	 * reverse a String
	 * Strings cannot be changed IN PLACE ?
	 */
	public static String reverse(String in){
		int len=in.length();
		StringBuilder sb=new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(in.charAt(len-i-1));
		}
		return sb.toString();
	}

	/**
	 * reverse a hex String representation 
	 * keeping the byte pairs together
	 * @param in
	 * @return
	 */
	public static String reverseHex(String in){
		int len=in.length();
		StringBuilder sb=new StringBuilder(len);
		for (int i = 0; i < len/2; i++) {
			sb.append(in.charAt(len-(2*i)-2));
			sb.append(in.charAt(len-(2*i)-1));
		}
		return sb.toString();
	}

	
}
