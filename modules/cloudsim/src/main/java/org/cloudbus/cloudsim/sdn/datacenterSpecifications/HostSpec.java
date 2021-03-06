/**
 * 
 */
package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import com.google.gson.Gson;

/**
 * @author Nitesh Dudhey
 *
 */
public class HostSpec implements Cloneable {
	String name;
	String type;
	int pes;
	long mips;
	int ram;
	long storage;
	long bw;
	int nums;
	long size;
	long loopbw;
	double looplat;
	
	public HostSpec(String name, String type, int pes, long mips, int ram, long storage, long bw, int nums, long size, long loopbw, double looplat){
		this.name = name;
		this.type = type;
		this.pes =  pes;
		this.mips = mips;
		this.ram = ram;
		this.storage = storage;
		this.bw = bw;
		this.nums = nums;
		this.size = size;
		this.loopbw = loopbw;
		this.looplat = looplat;
	}

	public String getName() {
		return name;
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

	public long getStorage() {
		return storage;
	}

	public void setStorage(long storage) {
		this.storage = storage;
	}

	public long getBw() {
		return bw;
	}

	public void setBw(long bw) {
		this.bw = bw;
	}

	public int getNums() {
		return nums;
	}

	public void setNums(int nums) {
		this.nums = nums;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getLoopBw() {
		return loopbw;
	}

	public void setLoopBw(long loopbw) {
		this.loopbw = loopbw;
	}

	public double getLoopLat() {
		return looplat;
	}

	public void setLoopLat(double looplat) {
		this.looplat = looplat;
	}
	
	public Object clone() throws CloneNotSupportedException {
    	return super.clone();
	}
	
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
