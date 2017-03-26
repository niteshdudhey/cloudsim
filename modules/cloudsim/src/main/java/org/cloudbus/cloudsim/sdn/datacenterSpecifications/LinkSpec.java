/**
 * 
 */
package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import com.google.gson.Gson;

/**
 * @author Nitesh Dudhey
 *
 */
public class LinkSpec {
	String source;
	String destination;
	double latency;
	
	public LinkSpec(String source, String destination, double latency){
		this.source = source;
		this.destination = destination;
		this.latency = latency;
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

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}
	
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
