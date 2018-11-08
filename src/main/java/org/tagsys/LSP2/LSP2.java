package org.tagsys.LSP2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;


/**
 * Verbindung zu einem Tagsys LSP2 Security Pedestal
 */
public class LSP2{
	
	String name="GateX";
	String lsip="";
	Integer lsport=4001; //default 
	Socket mysock = null; //neue Verbindung zum LSP2
	SocketAddress lsaddress=null;
	DataInputStream inStream = null;
	DataOutputStream outStream = null;
	Timer conntim = new Timer();
	TimerTask closetimertask=new TimerTask(){
		@Override
		public void run() {
			close();
		}
	};

	Integer lastcount=0;
	Date lastseen=null;
	Date lastlspdate=null;
	String status="init..";
	Integer lastDBsize =-1;

	
	/**
	 * 
	 * @param hostname
	 * @param port
	 */
	public LSP2(String hostname, Integer port) {
		lsip=hostname;
		lsport=port;
		mysock=new Socket();
	}

	/**
	 * Use default port
	 *  @param hostname 
	 */
	public LSP2(String hostname) {
		lsip=hostname;
		mysock=new Socket();
	}
	
	public Integer getcounter(){
		Integer count=0;
		byte[] byteres=	runcommand((byte)0x32); 
		if(null != byteres){
//		if(2 != byteres.length) return -1; // sollten genau 2 bytes sein
			
		for (byte b : byteres) {
			int u=(b<0 ? b+256 : b);
			count <<= 8;
			count+=u;		
		}	
		lastcount=count;
		status=String.format("(getcounter: %d) ", count);
		}else{
		//status="(getcounter: null result) ";
		}
		return count;
		
	}

	/**
	 * return the system time of the gate
	 * @return reads six numeric bytes MdYhms
	 */
	public  Date getTime(){
		byte[] da = runcommand((byte)0x0a); // six bytes: MDYhms
		if(null==da)return null;
		Date d=new Date(da[2]+100,da[0]-1,da[1],da[3],da[4],da[5]);
		lastlspdate=d; // remember
		return d;
	}
	
	/**
	 * format the system time according to ISO 2011-02-28-09-08-30  
	 * @return
	 */
	public String getTimeString(){
		// TODO use time formatting
		return getTime().toGMTString();
	}
	
	
	public int getDBSize(){
		byte[] res = runcommand((byte)0x2d);
		if(null==res)return 0;
		if(res.length<3) return 0; // happens?
		int count=0;
		for (int i = 0; i < 2; i++) {
			byte b=res[i];
			int u=(b<0 ? b+256 : b);
			count <<= 8;
			count+=u;		
		}
		lastDBsize=count;
		return count;
	}
	
	/**
	 * read Gate DbRecord 
	 * @param n
	 */
	public DBRecord getDBRecord(int n){
		return new DBRecord(getDBArray(n));
	}
	
	/**
	 * read Gate Db raw Record 
	 * @param n
	 */
	public byte[] getDBArray(int n){
		byte[] arg = new byte[2];
		arg[1]=(byte)(n & 0xff); //TODO what if that gets signed?
		n>>=8;
		arg[0]=(byte)(n & 0xff);
		byte[] res = runcommand((byte)0x2e, arg);
/*
		for (byte d : res) {
			System.err.format("%02x", (byte)d);			
		}
*/
		return res;
	}
	
	public String getName(){
		return name;
	}
	public void setName(String newname){
		name=newname;
	}
	public Date getLastSeen(){
		return lastseen;
	}
	public void setLastSeen(Date d){
		lastseen=d;
	}
	public Date getLastLSPDate(){
		return lastlspdate;
	}
	public Integer getLastDBsize(){
		return lastDBsize;
	}
	public Integer getLastCount(){
		return lastcount;
	}
	
	/**
	 * wrapper for commands w/o arguments
	 * @param cmd
	 * @return
	 */
	private byte[] runcommand(byte cmd){
		return runcommand(cmd,null);
	}
	
	/**
	 * communicate with the gate
	 * @param cmd - command verb as byte set - see LSP2 mnemonics
	 * @param argument - optional if wanted by cmd
	 * @return
	 */
	private synchronized byte[] runcommand(byte cmd, byte[] argument){
		byte[] result=null;
		Integer announcedlength=0;
		Integer readlength=0;
		byte[] command=new byte[3];
		command[0]=0x1e;
		command[1]=cmd;
		if(null==argument)
			command[2]=0x00;
		else{
			command[2]=(byte)argument.length;
		}
			
		// communicate command and read result
		if(!open())return null ; // connect failed 
			
		lastseen=new Date(); // keep this as last time we saw the LSP
		
		try{
		// send command
//			System.err.format("\nwrite%02x%02x%02x", command[0], command[1], command[2]);			
			outStream.write(command,0,3);
			
			if(null!=argument){
//				System.err.format("arg%02x%02x\n", argument[0], argument[1]);			
				outStream.write(argument); // wenn das mal gutgeht
			}
			
		// get datalength
			result=new byte[3];
			inStream.read(result, 0, 2);
			if ((command[0]==result[0]) && (command[1]==result[1])){  // echoed my command OK
				announcedlength=inStream.read();	
//				System.err.format("announcedlength-here %d bytes\n", announcedlength);
			};
		
		// get data
			result=new byte[announcedlength];
			while(readlength<announcedlength){ // read until all bytes are here (what if waiting in vain?)
				readlength+=inStream.read(result,readlength,announcedlength-readlength);
//				System.err.format("read %d bytes so far\n", readlength);
			}
//			return result;
		}
		catch(ConnectException e){
			status=e.toString();
		}
		catch(IOException e){
			//TODO gib Laut
			System.err.printf("runcommand-err: %s",e.toString());
			status= String.format("runcommand-status: %s", e.toString());

		}
//		close(); // closed on timeout if we forget here

		return result; 
	}
	
	private void close() {

		if(null==mysock){
			status="close hit null socket";
			return; // was never open
		}
		try{
			status="connection killed";
			inStream.close();
			outStream.close();
			mysock.close();
			mysock=null;
//			conntim.purge(); // ? evtl.
		}
		catch(IOException e){
			status=String.format("close failed: %s", e.toString());
		}
		System.err.println("close: called");
	}
	
	/**
	 * @return true if currently connected - without connecting!
	 * 
	 */
	public boolean isConnected(){
		return (null!=mysock && mysock.isConnected());
	}

	public String getStatus(){
		return status;	
	}
	
	/**
	 * try to connect, produce valid DataInputStream
	 * @return true on success
	 */
	private boolean open() { 
		// TODO Auto-generated method stub
		int triesleft=3;
		
		final int randmillis=200;
//		conntim.purge(); //?
		closetimertask.cancel();
		
		status="connect: start";
		if(null==mysock){
			mysock=new Socket();
		}
		
		while((null==mysock)
			||!mysock.isConnected()){
		try{
			mysock.connect(new InetSocketAddress(lsip,lsport)); // reconnect the same socket will fail after close
			
//			mysock = new Socket(lsip, lsport);

			inStream=new DataInputStream(mysock.getInputStream());
			outStream=new DataOutputStream(mysock.getOutputStream());
		}
		catch(IllegalArgumentException e){
			triesleft--;
//			e.printStackTrace();
			System.err.printf("%d tries left: %s\n",triesleft,e.toString());
			if(triesleft<=0){
				status= String.format("conn failed: %s", e.toString());
				return false;
			}
		}
		catch(ConnectException e){
			triesleft=0;
			status= String.format("conn failed: %s", e.toString());
			return false;
		}
		catch(IOException e){
			triesleft--;
			System.err.printf("%d tries left: %s",triesleft,e.toString());
//			System.err.print(e.toString());
//			e.printStackTrace();
			
			if(triesleft<=0){
				status= String.format("conn failed: %s", e.toString());
				return false;
			}							
		}

		try{
			
			Thread.sleep((int)(100+randmillis*Math.random()),1);
//			Thread.sleep(500,1);
		}
		catch(InterruptedException e){
			// auch egal
		}
		
		}
		status="connected";
		
		closetimertask=new TimerTask(){
			@Override
			public void run() {
				close();
			}
		};
//		conntim.schedule(closetimertask, 10000); 
		
		return true; 
	}

	/**
	 * make a hashmap out of me
	 * @return
	 */
	public HashMap<String,Object> toHashMap(){
		HashMap<String,Object> myhm= new HashMap<String,Object>();
		
		myhm.put("title",name);
		myhm.put("lsp2", this);
		myhm.put("ip", lsip);
		myhm.put("port", lsport);
		
		return myhm;
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		mysock.close(); // disconnect TCP
		conntim.cancel();
		super.finalize();
	}

}
