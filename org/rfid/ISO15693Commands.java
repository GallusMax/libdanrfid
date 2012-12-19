/**
 * 
 */
package org.rfid;

/**
 * @author uhahn
 *
 */
public interface ISO15693Commands {

	public byte[] inventory(); //TODO find out about the return type
	public byte[] stayQuiet(byte[] UID);
	public byte[] readSingleBlock(int b);

	/**
	 * write one block
	 * @param i - the block number
	 * @param data - the bytes to be written
	 * @return the transceive result
	 */
	public byte[] writeSingleBlock(int i,byte[] data);

	public byte[] lockBlock(int b);
	
	/**
	 * 
	 * @param i starting block number
	 * @param j block count
	 * @return block content concatenated 
	 */
	public byte[] readMultipleBlocks(int i,int j);
	
	/**
	 * write several blocks 
	 * @param i index of starting block
	 * @param n number of blocks to be written
	 * @param data byte array containing the data to be written
	 * @return the error code
	 */
	public byte[] writeMultipleBlock(int i,int n,byte[] data);

	public byte[] select(byte[] UID);
	public byte[] resetToReady();
	
	public byte[] writeAFI(byte newAFI);
	public byte[] lockAFI(byte newAFI);

	public byte[] writeDSFID(byte newAFI);
	public byte[] lockDSFID(byte newAFI);

	public byte[] getSystemInformation(byte newAFI);
	public byte[] getMultipleBlockSecurityStatus(byte newAFI);

	public byte[] write2Blocks(int i,byte[] data);
	public byte[] lock2Blocks(int i,byte[] data);

	public byte[] kill(byte[] UID);

	public byte[] writeSingleBlockPassword(int b,byte[] passwd,byte[] data);

}
	
