package org.cloudbus.cloudsim.sdn;

public class VSwitch {
	
	private String name;
	
	private double startTime;
	
	private double finishTime;
	
	private int datacenterId;
	
	private int bw;
	
	private long iops;
	
	private int currentupports = 0;
	
	private int currentdownports = 0;
	
	public VSwitch(String name, int bw, long iops, int upports, int downports, 
					double startTime, double finishTime, int datacenterId) {
		this.name = name;
		this.bw = bw;
		this.iops = iops;
		this.currentupports = upports;
		this.currentdownports = downports;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.datacenterId = datacenterId;
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
	
	public int currentUpports(){
		return currentupports;
	}
	
	public int currentDownports(){
		return currentdownports;
	}
	
	public void storeCurrentState(double time) {
		
	}
	
}
