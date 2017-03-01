package org.cloudbus.cloudsim;

import java.util.Collection;
import java.util.List;

import org.cloudbus.cloudsim.sdn.Link;
import org.cloudbus.cloudsim.sdn.SDNHost;

import com.google.common.collect.Table;

public class EventSummary {
	
	private static List<? extends Vm> vmList;
	
	private static List<Host> hostList;
	
	private static List<SDNHost> sdnHostList;
	
	private static Collection<Link> links;
		
	public EventSummary(List<Vm> vmList, List<Host> hostList) {
		EventSummary.vmList = vmList;
		EventSummary.hostList = hostList;
		System.out.println("Event Summary Class Output");
		System.out.println("==========================");
		for (Vm vm: vmList) {
			System.out.println(vm.toString());
		}
		for (Host host: hostList) {
			System.out.println(host.toString());
		}
	}
	
	public static void storePresentState(double time) {
		if (vmList != null) {
			for (Vm vm: vmList) {
				vm.storeCurrentState(time);
			}
		}
		if (hostList != null) {
			for (Host host: hostList) {
				host.storeCurrentState(time);
			}
		}
		if (sdnHostList != null) {
			for (SDNHost host: sdnHostList) {
				host.storeCurrentState(time);
			}
		}
		if (links != null) {
			for (Link link: links) {
				link.storeCurrentState(time);
			}
		}
	}
		
	public static void setVmList(List<? extends Vm> vmList) {
		System.out.println("Event Summary Class VMs");
		System.out.println("==========================");
		EventSummary.vmList = vmList;
		for (Vm vm: vmList) {
			if (vm != null) {
				System.out.println("VM ID = " + vm.getId());
			} else {
				System.out.println("Null VM in vmlist");
			}
		}
	}
	
	public static void setHostList(List<Host> hostList) {
		System.out.println("Event Summary Class Hosts");
		System.out.println("==========================");
		EventSummary.hostList = hostList;
		for (Host host: hostList) {
			if (host != null) {
				System.out.println("Host ID = " + host.getId());
			} else {
				System.out.println("Null Host in vmlist");
			}
		}
	}
	
	public static void setSDNHostList(List<SDNHost> sdnHostList) {
		System.out.println("Event Summary Class SDNHosts");
		System.out.println("==========================");
		EventSummary.sdnHostList = sdnHostList;
		for (SDNHost host: sdnHostList) {
			if (host != null) {
				System.out.println("Host ID = " + host.getName());
			} else {
				System.out.println("Null Host in vmlist");
			}
		}
	}
	
	public static void setLinks(Collection<Link> links) {
		System.out.println("Event Summary Class Links");
		System.out.println("=========================");
		EventSummary.links = links;
		for (Link link: links) {
			if (link != null) {
				System.out.println("Link ID = " + link.getName());
			} else {
				System.out.println("Null Host in vmlist");
			}
		}
	}
	
	public List<? extends Vm> getVmList() {
		return vmList;
	}
	
	public List<Host> getHostList() {
		return hostList;
	}
	
	public List<SDNHost> getSDNHostList() {
		return sdnHostList;
	}
	
	public Collection<Link> getLinks() {
		return links;
	}
}
