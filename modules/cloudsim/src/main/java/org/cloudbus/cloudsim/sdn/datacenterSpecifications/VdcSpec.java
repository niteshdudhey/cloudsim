package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class VdcSpec {

	double starttime;
	
	double endtime;
	
	List<VmSpec> vms;
	
	List<VLinkSpec> links;
	
	List<VSwitchSpec> vSwitches;
	
	public VdcSpec(List<VmSpec> vms, List<VLinkSpec> links, double starttime, double endtime) {
		this.vms = vms;
		this.links = links;
		this.starttime = starttime;
		this.endtime = endtime;
	}
	
	public VdcSpec() {
		vms = new ArrayList<VmSpec>();
		links = new ArrayList<VLinkSpec>();
		vSwitches = new ArrayList<VSwitchSpec>();
		starttime = 0;
		endtime = Double.POSITIVE_INFINITY;
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

	public List<VmSpec> getVms() {
		return vms;
	}

	public void setVms(List<VmSpec> vms) {
		this.vms = vms;
	}

	public List<VLinkSpec> getLinks() {
		return links;
	}

	public void setLinks(List<VLinkSpec> links) {
		this.links = links;
	}

	public List<VSwitchSpec> getVSwitches() {
		return vSwitches;
	}

	public void setVSwitches(List<VSwitchSpec> vSwitches) {
		this.vSwitches = vSwitches;
	}

	public void addVm(VmSpec vm) {
		vms.add(vm);
	}

	public void addLink(VLinkSpec link) {
		links.add(link);
	}

	public void addSwitch(VSwitchSpec vSwitch) {
		vSwitches.add(vSwitch);
	}
	
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}