package org.cloudbus.cloudsim.sdn;

public class LinkStateHistoryEntry {
	
	double time;
	
	double availableBw;
	
	public LinkStateHistoryEntry(double time, double availableBw) {
		this.time = time;
		this.availableBw = availableBw;
	}
	
	public String toString() {
		String str = "";
		str += "Available BW = " + Double.toString(availableBw);
		str += "\n";
		return str;
	}
	
	public double getTime() {
		return time;
	}
	
	public void setTime(double time) {
		this.time = time;
	}
	
	public double getAvailableBw() {
		return availableBw;
	}
	
	public void setAvailableBw(double availableBw) {
		this.availableBw = availableBw;
	}
}
