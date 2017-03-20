/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VLinkSpec;

import com.google.gson.Gson;

/**
 * Traffic requirements between two VMs
 * 
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */
public class Arc {

	int srcId;
	
	int dstId;
	
	int flowId;
	
	long requiredBandwidth;
	
	double requiredLatency;
	
	String name;
	
	public Arc(String name, int srcId, int dstId, int flowId, long reqBW, double reqLatency) {
		super();
		this.name = name;
		this.srcId = srcId;
		this.dstId = dstId;
		this.flowId = flowId;
		this.requiredBandwidth = reqBW;
		this.requiredLatency = reqLatency;
	}

	public Arc(VLinkSpec vLinkSpec, int srcId, int dstId, int flowId) {
		super();
		this.name = vLinkSpec.getName();
		this.srcId = srcId;
		this.dstId = dstId;
		this.flowId = flowId;
		// TODO: whether Bw is long or double ?
		this.requiredBandwidth = (long) vLinkSpec.getBw();
		this.requiredLatency = vLinkSpec.getLatency();
	}
	
	public String getName(){
		return name;
	}
	
	public int getSrcId() {
		return srcId;
	}

	public int getDstId() {
		return dstId;
	}
	public int getFlowId() {
		return flowId;
	}

	public long getBw() {
		return requiredBandwidth;
	}

	public double getLatency() {
		return requiredLatency;
	}
	
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
