/**
 * 
 */
package org.rfid.libdanrfid;

import java.util.ArrayList;

/**
 * 
 * Perform checks on RFID user data.
 * Currently: valid CRC, byte order within blocks, valid ISIL, plausibility of counts, valid VersionUsage
 * @author uhahn
 * 
 */
public class DDMfindings {

	protected DDMData ddd;
	
	
	protected ArrayList<String> report = new ArrayList<String>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2940980778101089498L;};
	protected double good=1;
	protected double threshold = 0.5;
	
	protected class DDMReport{
		protected String report;
		
		DDMReport(String in){
			report=in;
		}
	}

	/**
	 * @param d - current tag data
	 */
	public DDMfindings(DDMData d){
		ddd=d;
		good *= checkCRC();
		checkReverse();
		good *= checkISIL();
		good *= checkPlausibleCount();
		good *= checkType();
	}
	
	/**
	 * 
	 * @return ArrayList of human readable Strings
	 */
	public ArrayList<String> report(){
		return report;
	}
	
	/**
	 * 
	 * @return the plausibility
	 */
	public double good(){
		return good;
	}
	
	/**
	 * 
	 * @return the current plausibility threshold
	 */
	public double threshold(){
		return threshold;
	}
	
	/**
	 * modify plausibility threshold
	 * @param newvalue - the new value
	 */
	public void setthreshold(double newvalue){
		threshold=newvalue;
	}
	
	/**
	 * 
	 * @return true, if Tag is plausible enough
	 */
	public boolean trusted(){
		return good>threshold;
	}
	
	
	private double checkCRC(){
		double v=1;
		if(!ddd.compareCRC()){ // Attribut foundCRCok wird nur beim instantiieren gesetzt..
			report.add("CRC fehlt");
			v=0.8;
		}
		return v;
	}

	private void checkReverse(){
		if(ddd.reversed) report.add("revertierte Blöcke");
		
	}
	
	private double checkISIL(){
		double v=1;
		if(!ddd.Country().startsWith(" ")
			&& ddd.ISIL().startsWith(ddd.Country())) 
			report.add(ddd.Country()+" in ISIL wiederholt");
		if(!ddd.Country().matches("[A-Z]+")){
			report.add("Countrycode "+ddd.Country());
			v=0.7;
		}
		return v;
	}
	
	private double checkPlausibleCount(){
		double v=1;
		if(ddd.getPartNum()>ddd.getofParts()){
			report.add("Anzahl Items kleiner als Itemnummer");
			v=0.5;
		}
		if(ddd.getofParts()>7){
			v*= 3/(ddd.getofParts() - 4); // less with growing number of items
			report.add("zuviele Items: "+ddd.getofParts());
		}
		return v;
	}
	
	private double checkType(){
		double v=1;
		switch (ddd.getcharVersionUsage()){ // TODO - why not with static?
				case DDMData.V1AUSLEIHBAR: break;
				case DDMData.V1GELOESCHT: break;
				case DDMData.V1KUNDE: break;
				case DDMData.V1NEU: break;
				case DDMData.V1PRAESENZ: break;
				default: report.add(String.format("Typfeld unbekannt: 0x%h",ddd.getcharVersionUsage()));
						v=0.5;
		}
		return v;
	}
	
}
