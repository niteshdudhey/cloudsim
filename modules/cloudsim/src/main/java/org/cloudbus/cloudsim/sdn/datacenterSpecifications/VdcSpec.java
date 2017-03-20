package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import java.util.List;

import com.google.gson.Gson;

public class VdcSpec {

	double starttime;
	
	double endtime;
	
	List<VmSpec> vms;
	
	List<VLinkSpec> links;
	
	public VdcSpec(List<VmSpec> vms, List<VLinkSpec> links, double starttime, double endtime){
		this.vms = vms;
		this.links = links;
		this.starttime = starttime;
		this.endtime = endtime;
	}
	
	public double getStarttime() {
		return starttime;
	}

	public double getEndtime() {
		return endtime;
	}

	public List<VmSpec> getVms() {
		return vms;
	}

	public List<VLinkSpec> getLinks() {
		return links;
	}

	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}