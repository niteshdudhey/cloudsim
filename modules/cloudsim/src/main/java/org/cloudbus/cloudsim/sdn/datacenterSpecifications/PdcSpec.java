package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import java.util.List;

import com.google.gson.Gson;

public class PdcSpec {

	List<HostSpec> hosts;
	
	List<SwitchSpec> switches;
	
	List<LinkSpec> links;
	
	public PdcSpec(List<HostSpec> hosts, List<SwitchSpec> switches, List<LinkSpec> links){
		this.hosts = hosts;
		this.switches = switches;
		this.links = links;
	}

	public List<HostSpec> getHosts() {
		return hosts;
	}

	public void setHosts(List<HostSpec> hosts) {
		this.hosts = hosts;
	}

	public List<SwitchSpec> getSwitches() {
		return switches;
	}

	public void setSwitches(List<SwitchSpec> switches) {
		this.switches = switches;
	}

	public List<LinkSpec> getLinks() {
		return links;
	}

	public void setLinks(List<LinkSpec> links) {
		this.links = links;
	}
	
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
