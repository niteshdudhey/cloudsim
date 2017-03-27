package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import com.google.gson.Gson;

/**
 * @author Nitesh Dudhey
 *
 */
public class VSwitchSpec {
	String name;
	String type;
	double starttime;
	double endtime;
	int bw;
	long iops;
	int nums;
	int upports;
	int downports;
	String pswitch;
	
	public VSwitchSpec(String name, String type, int bw, long iops, int nums, int upports, int downports, 
			double startTime, double finishTime, String pswitchName) {
		this.name = name;
		this.type = type;
		this.bw = bw;
		this.iops = iops;
		this.nums = nums;
		this.upports = upports;
		this.downports = downports;
		this.starttime = startTime;
		this.endtime = finishTime;
		this.pswitch = pswitchName;
	}

	public String getName() {
		return name;
	}

	public int getNums() {
		return nums;
	}

	public void setNums(int nums) {
		this.nums = nums;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getIops() {
		return iops;
	}

	public void setIops(long iops) {
		this.iops = iops;
	}

	public int getBw() {
		return bw;
	}

	public void setBw(int bw) {
		this.bw = bw;
	}

	public double getStarttime() {
		return starttime;
	}

	public void setStarttime(double starttime) {
		this.starttime = starttime;
	}

	public double getEndtime() {
		return endtime;
	}

	public void setEndtime(double endtime) {
		this.endtime = endtime;
	}
	
	public String getPSwitchName() {
		return pswitch;
	}
	
	public void setPSwitchName(String pswitch) {
		this.pswitch = pswitch;
	}
	
	public int getUpports() {
		return upports;
	}
	
	public int getDownports() {
		return downports;
	}
	
	public void setUpports(int upports) {
		this.upports = upports;
	}
	
	public void setDownports(int downports) {
		this.downports = downports;
	}
	
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
}
