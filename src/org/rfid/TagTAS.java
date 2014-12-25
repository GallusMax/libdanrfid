package org.rfid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

//import org.android.nfc.tech.Util;

public class TagTAS implements Runnable{
	protected static URL urlTas;
	protected static String strbaseUrlTas;
	protected static boolean urlValid=false;
	private String UID=null;
	private String barcode=null;
	private String userdata=null;
	
	/**
	 * prepares a connect to the given URL
	 * TODO throw Exception when URL is not valid
	 * @param newurl
	 */
	public TagTAS(String newurl) {
		strbaseUrlTas=newurl;
		try {
			urlTas = new URL(newurl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			urlValid=false;
		}
		urlValid=true;
		
		
	}

	public TagTAS(){
		this("http://yourserver.home.at/cgi-bin/tagtas.cgi"); 
		/* answers with string <UIDhex>:<barcode> on CGI parameters
		 * id=<the tag UID in hex>
		 * bar=<the new barcode> - optional, if an update is wanted
		 */
		
	}
	
	public TagTAS(String url, String UID) {
		this(url);
		this.UID=UID;
		
	}

	public TagTAS(String url, String UID, String bar) {
		this(url);
		this.UID=UID;
		this.barcode=bar;
	}
	
	public void addUserData(String in){
		userdata=in;
	}

	@Override
	public void run() {
		if(null==barcode) // request only 
			barcode(UID);
		else
			updatebarcode(UID, barcode);
		if(null!=userdata){ // we have additional data
			updateudat(UID,userdata);
		}
	}


	/**
	 * fetches the barcode from the db - this may take some time
	 * @param UID - the tag UID. Ordering will be guessed from "e004" beginning
	 * @return - the OLD barcode, if any. remember: the NEW barcode will be added to the db
	 * TODO what about caching?
	 */
	public String barcode(String UID){
		try {
			String res=dorequest(strbaseUrlTas + "?id=" + UID);
			return res;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//TODO valid error reply		
		return "request failed";
	}
	
	/**
	 * updates the barcode on the db - this may take some time
	 * @param UID - the tag UID. Ordering will be guessed from "e004" beginning
	 * @param barcode - the new barcode
	 * @return - the OLD barcode, if any. remember: the NEW barcode will be added to the db
	 * TODO what about caching?
	 */
	public String updatebarcode(String UID, String barcode){
		try {
			return dorequest(strbaseUrlTas + "?id=" + UID + "&bar=" + URLEncoder.encode(barcode));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "update failed";
	}

	/**
	 * updates the userdata on the db - this may take some time
	 * @param UID - the tag UID. Ordering will be guessed from "e004" beginning
	 * @param udat - the raw userdata from tag
	 * @return - the barcode from db, if any. 
	 * TODO what about caching?
	 */
	public String updateudat(String UID, String udat){
		try {
			return dorequest(strbaseUrlTas + "?id=" + UID + "&udat=" + URLEncoder.encode(udat));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "update udat failed";
	}
	
	protected String dorequest(String urlquery) throws IOException{
		String strResult="";
		URL CgiUrlTas;
//		System.err.println("dorequest: "+urlquery);
		try {
			CgiUrlTas = new URL(urlquery); // TODO unchecked?
			InputStream is = (InputStream)CgiUrlTas.getContent();

			byte[] bytes = new byte[256];
//			int i=0, readlength=0;
//			while(0<(readlength=is.read(bytes,i,256-i)))
//				i+=readlength;
			int readlength = is.read(bytes);

			for(int i=0;i<readlength;i++)
				strResult=strResult.concat(String.format("%c",bytes[i]));
		} 
		catch (MalformedURLException mue) {
			// TODO Auto-generated catch block
			mue.printStackTrace();
		}
		return strResult;
	}

}
