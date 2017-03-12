package org.cloudbus.cloudsim.sdn;

public class VSwitchStateHistoryEntry {

	double time;
	
	long numPackets;
	
	public VSwitchStateHistoryEntry(double time, long numPackets) {
		this.time = time;
		this.numPackets = numPackets;
	}
	
	public double getTime() {
		return time;
	}
	
	public void setPacketsTransferred(long numPackets) {
		this.numPackets = numPackets;
	}
	
	public long getPacketsTransferred() {
		return numPackets;
	}
	
}
