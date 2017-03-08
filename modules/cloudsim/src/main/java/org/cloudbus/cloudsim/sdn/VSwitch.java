package org.cloudbus.cloudsim.sdn;

public class VSwitch {
	
	private String name;
	
	private double startTime;
	
	private double finishTime;
	
	private int datacenterId;
	
	private int bw;
	
	private long iops;
	
	private int upports = 0;
	
	private int downports = 0;
	
	private Switch pswitch;
	
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
	
	public void storeCurrentState(double time) {
		
	}
	
}
