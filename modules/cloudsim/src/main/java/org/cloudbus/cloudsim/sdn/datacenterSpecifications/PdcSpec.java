package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import java.util.ArrayList;
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
	
	public PdcSpec(){
		hosts = new ArrayList<HostSpec>();
		switches = new ArrayList<SwitchSpec>();
		links = new ArrayList<LinkSpec>();
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
	
	public void addHost(HostSpec host) {
		hosts.add(host);
	}

	public void addSwitch(SwitchSpec switch1) {
		switches.add(switch1);
	}

	public void addLink(LinkSpec link) {
		links.add(link);
	}

	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
