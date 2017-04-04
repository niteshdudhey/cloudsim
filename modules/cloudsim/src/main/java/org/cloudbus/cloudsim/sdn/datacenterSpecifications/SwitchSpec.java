/**
 * 
 */
package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import com.google.gson.Gson;

/**
 * @author Nitesh Dudhey
 *
 */
public class SwitchSpec implements Cloneable {
	String name;
	String type;
	int bw;
	long iops;
	int upports;
	int downports;
	double delay;
	
	public SwitchSpec(String name, String type, int bw, long iops, int upports, int downports, double delay) {
		this.name = name;
		this.type = type;
		this.bw = bw;
		this.iops = iops;
		this.upports = upports;
		this.downports = downports;
		this.delay = delay;
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

	public int getBw() {
		return bw;
	}

	public void setBw(int bw) {
		this.bw = bw;
	}

	public long getIops() {
		return iops;
	}

	public void setIops(long iops) {
		this.iops = iops;
	}

	public int getUpports() {
		return upports;
	}

	public void setUpports(int upports) {
		this.upports = upports;
	}

	public int getDownports() {
		return downports;
	}

	public void setDownports(int downports) {
		this.downports = downports;
	}
	
	public double getSwitchingDelay() {
		return delay;
	}

	public void setSwitchingDelay(double delay) {
		this.delay = delay;
	}
	
	public Object clone() throws CloneNotSupportedException {
    	return super.clone();
	}
	
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
