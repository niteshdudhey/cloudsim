/**
 * 
 */
package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import com.google.gson.Gson;

/**
 * @author Nitesh Dudhey
 *
 */
public class PDCSpecShort {
	int coreSwitchFanout;
	SwitchSpec coreSwitchSpec;
	
	int aggregateSwitchFanout;
	SwitchSpec aggregateSwitchSpec;
	
	int edgeSwitchFanout;
	SwitchSpec edgeSwitchSpec;
	
	HostSpec hostSpec;
	
	LinkSpec linkSpec;

	public int getCoreSwitchFanout() {
		return coreSwitchFanout;
	}

	public void setCoreSwitchFanout(int coreSwitchFanout) {
		this.coreSwitchFanout = coreSwitchFanout;
	}

	public SwitchSpec getCoreSwitchSpec() {
		return coreSwitchSpec;
	}

	public void setCoreSwitchSpec(SwitchSpec coreSwitchSpec) {
		this.coreSwitchSpec = coreSwitchSpec;
	}

	public int getAggregateSwitchFanout() {
		return aggregateSwitchFanout;
	}

	public void setAggregateSwitchFanout(int aggregateSwitchFanout) {
		this.aggregateSwitchFanout = aggregateSwitchFanout;
	}

	public SwitchSpec getAggregateSwitchSpec() {
		return aggregateSwitchSpec;
	}

	public void setAggregateSwitchSpec(SwitchSpec aggregateSwitchSpec) {
		this.aggregateSwitchSpec = aggregateSwitchSpec;
	}

	public int getEdgeSwitchFanout() {
		return edgeSwitchFanout;
	}

	public void setEdgeSwitchFanout(int edgeSwitchFanout) {
		this.edgeSwitchFanout = edgeSwitchFanout;
	}

	public SwitchSpec getEdgeSwitchSpec() {
		return edgeSwitchSpec;
	}

	public void setEdgeSwitchSpec(SwitchSpec edgeSwitchSpec) {
		this.edgeSwitchSpec = edgeSwitchSpec;
	}

	public HostSpec getHostSpec() {
		return hostSpec;
	}

	public void setHostSpec(HostSpec hostSpec) {
		this.hostSpec = hostSpec;
	}

	public LinkSpec getLinkSpec() {
		return linkSpec;
	}

	public void setLinkSpec(LinkSpec linkSpec) {
		this.linkSpec = linkSpec;
	}
	
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
