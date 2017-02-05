package org.cloudbus.cloudsim;

import java.util.List;

public class FullHostStateHistoryEntry extends HostStateHistoryEntry {
	
	int ram;
	
	int availableRam;
	
	int requestedRam;
	
	/*
	 * Total Mips of all Pes
	 */
	double mips;
	
	/*
	 * Total available mips over all Pes of the host
	 */
	double availableMips;
	
	/*
	 * Available mips in each Pe of the host
	 */
	List<Double> availableMipsList;
	
	double requestedMips;
	
	long bw;
	
	long availableBw;
	
	long requestedBw;
	
	double cpuUtil;
	
	double ramUtil;
	
	double bwUtil;
	
	List<Integer> vmIdsList;
	
	public FullHostStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isActive) {
		super(time, allocatedMips, requestedMips, isActive);
	}
	
	public String toString() {
		String str = "";
		str += "Ram = " + Integer.toString(ram) + "; Available Ram = " + Integer.toString(availableRam)
				+ "; Requested Ram = " + Integer.toString(requestedRam) + "\n";
		str += "Mips = " + Double.toString(mips) + "; Available Mips = " + Double.toString(availableMips)
				+ "; Requested Mips = " + Double.toString(requestedMips) + "\n";
		str += "Bw = " + Long.toString(bw) + "; Available Bw = " + Long.toString(availableBw)
				+ "; Requested Bw = " + Long.toString(requestedBw) + "\n";
		str += "CPU Util = " + Double.toString(cpuUtil) + "; Ram Util = " + Double.toString(ramUtil)
				+ "; Bw Util = " + Double.toString(bwUtil) + "\n";
		str += "VM IDs = ";
		for (Integer id: vmIdsList) {
			str += Integer.toString(id) + " ";
		}
		str += "\n";
		return str;
	}
	
	public int getRam() {
		return ram;
	}
	
	public void setRam(int ram) {
		this.ram = ram;
	}
	
	public int getAvailableRam() {
		return availableRam;
	}
	
	public void setAvailableRam(int ram) {
		this.availableRam = ram;
	}
	
	public int getRequestedRam() {
		return requestedRam;
	}
	
	public void setRequestedRam(int ram) {
		this.requestedRam = ram;
	}
	
	public double getMips() {
		return mips;
	}
	
	public void setMips(double mips) {
		this.mips = mips;
	}
	
	public double getAvailableMips() {
		return availableMips;
	}
	
	public void setAvailableMips(double mips) {
		this.availableMips = mips;
	}
	
	public double getRequestedMips() {
		return requestedMips;
	}
	
	public void setRequestedMips(double mips) {
		this.requestedMips = mips;
	}
	
	public List<Double> getAvailableMipsList() {
		return availableMipsList;
	}
	
	public void setAvailableMipsList(List<Double> mipsList) {
		this.availableMipsList = mipsList;
	}
	
	public long getBw() {
		return bw;
	}
	
	public void setBw(long bw) {
		this.bw = bw;
	}
	
	public long getAvailableBw() {
		return availableBw;
	}
	
	public void setAvailableBw(long bw) {
		this.availableBw = bw;
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
	
	public List<Integer> getVmIdsList() {
		return vmIdsList;
	}
	
	public void setVmIdsList(List<Integer> vmIdsList) {
		this.vmIdsList = vmIdsList;
	}

}
