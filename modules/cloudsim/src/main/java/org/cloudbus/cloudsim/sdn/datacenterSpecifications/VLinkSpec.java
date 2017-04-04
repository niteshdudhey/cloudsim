package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import com.google.gson.Gson;

public class VLinkSpec implements Cloneable {

	String name;
	String source;
	String destination;
	double latency;
	double bw;
	
	public VLinkSpec(String name, String source, String destination, long bw){
		this.name = name;
		this.source = source;
		this.destination = destination;
		this.bw = bw;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public double getBw() {
		return bw;
	}

	public void setBw(double bw) {
		this.bw = bw;
	}
	
	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

	public Object clone() throws CloneNotSupportedException {
    	return super.clone();
	}
	
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
