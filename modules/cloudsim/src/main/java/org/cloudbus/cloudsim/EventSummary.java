package org.cloudbus.cloudsim;

import java.util.List;

public class EventSummary {
	
	private static List<? extends Vm> vmList;
	
	private static List<Host> hostList;
		
	public EventSummary(List<Vm> vmList, List<Host> hostList) {
		EventSummary.vmList = vmList;
		EventSummary.hostList = hostList;
	}
	
	public static void storePresentState(double time) {
		for (Vm vm: vmList) {
			vm.storeCurrentState(time);
		}
		for (Host host: hostList) {
			host.storeCurrentState(time);
		}
	}
	
	public static void setVmList(List<? extends Vm> vmList) {
		EventSummary.vmList = vmList;
	}
	
	public static void setHostList(List<Host> hostList) {
		EventSummary.hostList = hostList;
	}
	
	public List<Vm> getVmList(List<Vm> vmList) {
		return vmList;
	}
	
	public List<Host> getHostList(List<Host> hostList) {
		return hostList;
	}
	
}
