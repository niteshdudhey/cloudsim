/**
 * 
 */
package org.cloudbus.cloudsim.sdn;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Vm;

/**
 * Virtual topology of a Virtual Datacenter.
 * 
 * @author Nitesh Dudhey
 *
 */
public class VirtualTopology {
	
	String datacenter;
	
	int datacenterId;
	
	// Vm id -> Vm
	Map<Integer, Vm> vmsTable;
	
	// VSwitch id -> VSwitch
	Map<Integer, VSwitch> vswitchesTable;
	
	// VLink id (Flow id) -> Arc (VLink)
	Map<Integer, Arc> vlinksTable;
	
	public VirtualTopology(int datacenterId, String datacenter) {
		this.datacenterId = datacenterId;
		this.datacenter = datacenter;
		vmsTable = new HashMap<Integer, Vm>();
		vswitchesTable = new HashMap<Integer, VSwitch>();
		vlinksTable = new HashMap<Integer, Arc>();
	}
	
	public Map<Integer, Vm> getVmsTable() {
		return vmsTable;
	}

	public void addVm(Vm vm) {
		vmsTable.put(vm.getId(), vm);
	}
	
	public Vm getVmById(int id) {
		return vmsTable.get(id);
	}
	
	public Collection<Vm> getVms() {
		return vmsTable.values();
	}
	
	public Map<Integer, VSwitch> getVSwitchesTable() {
		return vswitchesTable;
	}

	public void addVSwitch(VSwitch vswitch) {
		vswitchesTable.put(vswitch.getId(), vswitch);
	}
	
	public VSwitch getVSwitchById(int id) {
		return vswitchesTable.get(id);
	}
	
	public Collection<VSwitch> getVSwitches() {
		return vswitchesTable.values();
	}
	
	public Map<Integer, Arc> getVLinksTable() {
		return vlinksTable;
	}

	public void addVSwitch(Arc arc) {
		vlinksTable.put(arc.getFlowId(), arc);
	}
	
	public Arc getVLinkById(int id) {
		return vlinksTable.get(id);
	}
	
	public Collection<Arc> getVLinks() {
		return vlinksTable.values();
	}
}
