/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.List;

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
	
	Node lowOrder;
	
	Node highOrder;

	int srcId;
	
	int dstId;
	
	int flowId;
	
	double requiredBandwidth;
	
	double requiredLatency;
	
	private List<Channel> upChannels;
	
	private List<Channel> downChannels;
	
	String name;
	
	public Arc(String name, int srcId, int dstId, int flowId, double reqBW, double reqLatency) {
		super();
		this.name = name;
		this.srcId = srcId;
		this.dstId = dstId;
		this.flowId = flowId;
		this.requiredBandwidth = reqBW;
		this.requiredLatency = reqLatency;
		this.upChannels = new ArrayList<Channel>();
		this.downChannels = new ArrayList<Channel>();
	}

	public Arc(VLinkSpec vLinkSpec, int srcId, int dstId, int flowId) {
		super();
		this.name = vLinkSpec.getName();
		this.srcId = srcId;
		this.dstId = dstId;
		this.flowId = flowId;
		this.requiredBandwidth = vLinkSpec.getBw();
		this.requiredLatency = vLinkSpec.getLatency();
		this.upChannels = new ArrayList<Channel>();
		this.downChannels = new ArrayList<Channel>();
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

	public double getBw() {
		return requiredBandwidth;
	}

	public double getLatency() {
		return requiredLatency;
	}
	
	public String toString() {
		String str = "";
		str += "Arc " + flowId + ": " + srcId + " <-> " + dstId;
		return str;
	}
}
