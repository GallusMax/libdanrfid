package org.rfid.libdanrfid;

public class TagData extends Object {

	protected int blocksize = 4;
	protected int nBlocks = 8;
	protected int nDMblocks = 8;
	protected char[] forward32 = new char[32];
	protected char[] userdata32 = forward32;
	protected char[] shadowdata;
	/**
	 * remember if we found reverted byteorder on tag
	 */
	protected boolean reversed = false;
	/**
	  * remember if the tag had a valid CRC when we read it
	  */
	protected boolean foundCRCok = false;
	/**
	 * the data format version, used on status updates
	 */
	protected char VERSION = 1;

	public TagData() {
		super();
	}

	/**
	 * check the given CRC 
	 * @return false if CRC do not match
	 */
	public boolean compareCRC() {
		int crc_sum=new CRC().DDCRC(userdata32);
		int found_crc=getCRC();
		return crc_sum==found_crc;
	}

	/**
	 * try to verify the CRC
	 * first try with given block order
	 * then reverse each 4 byte block and try again
	 * @return false if both block directions do not produce a valid CRC
	 * the block direction will be reverted again, if both do not match
	 */
	public boolean isvalid() {
		if(compareCRC())return true;
		reverseblocks();
		if(compareCRC())return true;
		reverseblocks(); // back again
		return false;
	}

	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index
	 * @return a byte array with the block 
	 * TODO : with respect to ordering? ;-)
	 */
	public byte[] getblock(int n) {
		return getblock(n,4,false); // default to blocksize 4
	}

	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index 
	 * @param forcereverse requests reverted byteorder (on empty tags)
	 * @return a byte array with the block 
	 */
	public byte[] getblock(int n, boolean forcereverse) {
		return getblock(n,4,forcereverse); // default to blocksize 4
	}

	/**
	 * prepare single block for writing.
	 * byteorder remains reversed, if found reversed during read
	 * @param n - the block index 
	 * @param blocksize - respect tag's blocksize
	 * @param forcereverse requests reverted byteorder (on empty tags)
	 * @return a byte array with the block 
	 */
	public byte[] getblock(int n, int blocksize, boolean forcereverse) {
		byte[] res= new byte[blocksize];
		
		for(int i=0;i<blocksize;i++)
			res[i]=(byte)((reversed||forcereverse) ? userdata32[(n+1)*blocksize-i-1] : userdata32[n*blocksize+i]);
		return res;
	}

	/**
	 * find out if our updates have changed block n
	 * @param n
	 * @return true if block needs to be written back to tag
	 */
	public boolean blocktainted(int n) {
		return blocktainted(n,4);
	}

	/**
	 * find out if our updates have changed block n
	 * @param n
	 * @param blocksize
	 * @return true if block needs to be written back to tag
	 */
	public boolean blocktainted(int n, int blocksize) {
		for(int i=0;i<blocksize;i++)
			if(bytetainted(n*blocksize+i)) 
				return true;
		return false;
	}

	/**
	 * change the order of each block 
	 * nb: changes order of blocks in place
	 * property "reversed" keeps track of operations
	 * FKI writes the blocks from 3 to 0, Biblioteca writes 0 to 3
	 */
	public void reverseblocks() { // TODO use actual blocksizes from tag system data
	/*
			int blocksize=4;
			int numblocks=8;
	*/
			for (int k = 0; k < nDMblocks; k++) { // all blocks
					reverse(userdata32,k*blocksize,blocksize); // swap string positions
			}
			reversed=(reversed?false:true); // toggle reversed marker
		}

	/**
	 * reverse a subset of an array, starting at @param offset 
	 * changing at max @param block bytes
	 * CHANGES THE ARRAY IN PLACE
	 */
	public char[] reverse(char[] in, int offset, int block) {
		char bt;
		for (int i = 0; i < block/2; i++) {
			bt=in[offset+i];
			in[offset+i]=in[offset+block-i-1];
			in[offset+block-i-1]=bt;
		}
		return in;
	}

	/**
	 * read the CRC as int, added up LSB first, MSB last
	 * @return
	 */
	public int getCRC() {
		int res=userdata32[20]&0xff;
		res<<=8;
		res+=userdata32[19]&0xff;
		return res;
	}

	/**
	 * 
	 * @return the blocksize according to Tag system info
	 */
	public int getblocksize() {
		return (int)blocksize;
	}

	/**
	 * 
	 * @return the number of blocks on the Tag
	 */
	public int getnBlocks() {
		return (int)nBlocks;
	}

	public void addUserData(byte[] in) {
		initdata(in);
	}

	/**
	 * compares a single byte between current array and original array
	 * @param i
	 * @return true if byte i was changed
	 */
	public boolean bytetainted(int i) {
		boolean istainted = userdata32[i]!=shadowdata[i];
		if(istainted)
			return istainted;
		else
			return istainted;
	}

	/**
	 * initialize the 32 byte user data array
	 * @param in - a byte array as read from
	 */
	protected void initdata(byte[] in) {
		for (int i = 0; i < 32; i++) { // dont trust in length ;-) 
			if(i<in.length) // any case..
				userdata32[i]=(char)in[i];
			else
				userdata32[i]=(char)0; // init all
		}
		// is this a crc ok Record?
		// nb: reverts the block byte order, if necessary
		foundCRCok=isvalid();
		
		keepshadowdata(); // keep this state in order to track the changes		
	}

	private void keepshadowdata() {
		shadowdata=userdata32.clone();
		for(int i=0;i<userdata32.length;i++)
			shadowdata[i]=(char)(userdata32[i]&0xff); // chars may have a sign?
	}

	/**
	 * put String @param s at postition @param start
	 * with at max @param len characters
	 */
	protected void setStringAt(String s, int start, int len) {
		for (int i = 0; i < len; i++) {
			if(i>=s.length()) // well - we fill in \0 here..
				userdata32[start+i]='\0';	
			else
				userdata32[start+i]=s.charAt(i);			
		}				
	}

}
