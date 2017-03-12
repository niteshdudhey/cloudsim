package org.cloudbus.cloudsim.sdn;

import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.FullVmStateHistoryEntry;

public class VSwitch {
	
	private String name;
	
	private double startTime;
	
	private double finishTime;
	
	private int datacenterId;
	
	private int bw;
	
	private long iops;
	
	private int upports = 0;
	
	private int downports = 0;
	
	private long numPackets;
	
	private Switch pswitch;
	
	private List<VSwitchStateHistoryEntry> stateHistory;
	
	public VSwitch(String name, int bw, long iops, int upports, int downports, 
					double startTime, double finishTime, int datacenterId, Switch pswitch) {
		this.name = name;
		this.bw = bw;
		this.iops = iops;
		this.upports = upports;
		this.downports = downports;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.datacenterId = datacenterId;
		this.pswitch = pswitch;
		this.numPackets = 0;
		this.stateHistory = new LinkedList<VSwitchStateHistoryEntry>();
	}
	
	public String getName() {
		return name;
	}
	
	public int getDatacenterId() {
		return datacenterId;
	}

	public double getStartTime() {
		return startTime;
	}
	
	public double getFinishTime() {
		return finishTime;
	}
	
	public int getBw() {
		return bw;
	}
	
	public long getIops() {
		return iops;
	}
	
	public int getUpports(){
		return upports;
	}
	
	public int getDownports(){
		return downports;
	}
	
	public Switch getSwitch() {
		return pswitch;
	}
	
	public void incrementNumPacketsTransferred(long num) {
		this.numPackets += num;
	}
	
	public long getNumPacketsTransferred() {
		return numPackets;
	}
	
	public List<VSwitchStateHistoryEntry> getVSwitchStateHistory() {
		return stateHistory;
	}
	
	public void storeCurrentState(double time) {
		VSwitchStateHistoryEntry stateHistory = new VSwitchStateHistoryEntry(time, getNumPacketsTransferred());
		getVSwitchStateHistory().add(stateHistory);
	}
	
}
