/**
 * 
 */
package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import com.google.gson.Gson;

/**
 * @author Nitesh Dudhey
 *
 */
public class VDCSpecShort {
	
	double starttime;
	
	double endtime;
	
	int coreSwitchFanout;
	VSwitchSpec coreSwitchSpec;
	
	int aggregateSwitchFanout;
	VSwitchSpec aggregateSwitchSpec;
	
	int edgeSwitchFanout;
	VSwitchSpec edgeSwitchSpec;
	
	VmSpec vmSpec;
	
	VLinkSpec linkSpec;

	
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

	public int getCoreSwitchFanout() {
		return coreSwitchFanout;
	}

	public void setCoreSwitchFanout(int coreSwitchFanout) {
		this.coreSwitchFanout = coreSwitchFanout;
	}

	public VSwitchSpec getCoreSwitchSpec() {
		return coreSwitchSpec;
	}

	public void setCoreSwitchSpec(VSwitchSpec coreSwitchSpec) {
		this.coreSwitchSpec = coreSwitchSpec;
	}

	public int getAggregateSwitchFanout() {
		return aggregateSwitchFanout;
	}

	public void setAggregateSwitchFanout(int aggregateSwitchFanout) {
		this.aggregateSwitchFanout = aggregateSwitchFanout;
	}

	public VSwitchSpec getAggregateSwitchSpec() {
		return aggregateSwitchSpec;
	}

	public void setAggregateSwitchSpec(VSwitchSpec aggregateSwitchSpec) {
		this.aggregateSwitchSpec = aggregateSwitchSpec;
	}

	public int getEdgeSwitchFanout() {
		return edgeSwitchFanout;
	}

	public void setEdgeSwitchFanout(int edgeSwitchFanout) {
		this.edgeSwitchFanout = edgeSwitchFanout;
	}

	public VSwitchSpec getEdgeSwitchSpec() {
		return edgeSwitchSpec;
	}

	public void setEdgeSwitchSpec(VSwitchSpec edgeSwitchSpec) {
		this.edgeSwitchSpec = edgeSwitchSpec;
	}

	public VmSpec getVmSpec() {
		return vmSpec;
	}

	public void setVmSpec(VmSpec vmSpec) {
		this.vmSpec = vmSpec;
	}

	public VLinkSpec getLinkSpec() {
		return linkSpec;
	}

	public void setLinkSpec(VLinkSpec linkSpec) {
		this.linkSpec = linkSpec;
	}
	
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
