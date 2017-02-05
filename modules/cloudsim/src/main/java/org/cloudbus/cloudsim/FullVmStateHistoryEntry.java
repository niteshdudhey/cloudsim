package org.cloudbus.cloudsim;

import java.util.List;

public class FullVmStateHistoryEntry extends VmStateHistoryEntry {
		
	int allocatedRam;
	
	int requestedRam;
		
	/*
	 * Total allocated mips over all Pes of the VM
	 */
	double allocatedMips;
	
	/*
	 * Available mips in each Pe of the VM
	 */
	List<Double> allocatedMipsList;
	
	double requestedMips;
		
	long allocatedBw;
	
	long requestedBw;
	
	double cpuUtil;
	
	double ramUtil;
	
	double bwUtil;
	
	public FullVmStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isInMigration) {
		super(time, allocatedMips, requestedMips, isInMigration);
	}
	
	public String toString() {
		String str = "";
		str += "Allocated Ram = " + Integer.toString(allocatedRam)
				+ "; Requested Ram = " + Integer.toString(requestedRam) + "\n";
		str += "Allocated Mips = " + Double.toString(allocatedMips)
				+ "; Requested Mips = " + Double.toString(requestedMips) + "\n";
		str += "Allocated Bw = " + Long.toString(allocatedBw)
				+ "; Requested Bw = " + Long.toString(requestedBw) + "\n";
		str += "CPU Util = " + Double.toString(cpuUtil) + "; Ram Util = " + Double.toString(ramUtil)
				+ "; Bw Util = " + Double.toString(bwUtil) + "\n";
		return str;
	}
	
	public int getAllocatedRam() {
		return allocatedRam;
	}
	
	public void setAllocatedRam(int ram) {
		this.allocatedRam = ram;
	}
	
	public int getRequestedRam() {
		return requestedRam;
	}
	
	public void setRequestedRam(int ram) {
		this.requestedRam = ram;
	}
	
	public double getAllocatedMips() {
		return allocatedMips;
	}
	
	public void setAllocatedMips(double mips) {
		this.allocatedMips = mips;
	}
	
	public double getRequestedMips() {
		return requestedMips;
	}
	
	public void setRequestedMips(double mips) {
		this.requestedMips = mips;
	}
	
	public List<Double> getAllocatedMipsList() {
		return allocatedMipsList;
	}
	
	public void setAllocatedMipsList(List<Double> mipsList) {
		this.allocatedMipsList = mipsList;
	}
	
	public long getAllocatedBw() {
		return allocatedBw;
	}
	
	public void setAllocatedBw(long bw) {
		this.allocatedBw = bw;
	}
	
	public long getRequestedBw() {
		return requestedBw;
	}
	
	public void setRequestedBw(long bw) {
		this.requestedBw = bw;
	}
	
	public double getCpuUtil() {
		return cpuUtil;
	}
	
	public void setCpuUtil(double cpuUtil) {
		this.cpuUtil = cpuUtil;
	}
	
	public double getRamUtil() {
		return ramUtil;
	}
	
	public void setRamUtil(double ramUtil) {
		this.ramUtil = ramUtil;
	}
	
	public double getBwUtil() {
		return bwUtil;
	}
	
	public void setBwUtil(double bwUtil) {
		this.bwUtil = bwUtil;
	}

}
