/**
 * 
 */
package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import com.google.gson.Gson;

/**
 * @author Nitesh Dudhey
 *
 */
public class VmSpec {
	String name;
	String type;
	long size;
	int pes;
	long mips;
	int ram;
	long bw;
	int nums;
	double starttime = -1;
	double endtime = -1;
	
	public VmSpec(String name, String type, long size, int pes, long mips, 
			int ram, int bw, double starttime, double endtime) {
		
		this.name = name;
		this.type = type;
		this.size = size;
		this.pes = pes;
		this.mips = mips;
		this.ram = ram;
		this.bw = bw;
		this.starttime = starttime;
		this.endtime = endtime;
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

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getPes() {
		return pes;
	}

	public void setPes(int pes) {
		this.pes = pes;
	}

	public long getMips() {
		return mips;
	}

	public void setMips(long mips) {
		this.mips = mips;
	}

	public int getRam() {
		return ram;
	}

	public void setRam(int ram) {
		this.ram = ram;
	}

	public long getBw() {
		return bw;
	}

	public void setBw(long bw) {
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
	
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
