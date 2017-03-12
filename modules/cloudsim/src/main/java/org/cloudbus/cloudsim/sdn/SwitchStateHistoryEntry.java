package org.cloudbus.cloudsim.sdn;

public class SwitchStateHistoryEntry {

	double time;
	
	long numPackets;
	
	public SwitchStateHistoryEntry(double time, long numPackets) {
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
