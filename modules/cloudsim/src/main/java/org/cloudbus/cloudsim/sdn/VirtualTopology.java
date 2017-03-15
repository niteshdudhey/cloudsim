/**
 * 
 */
package org.cloudbus.cloudsim.sdn;

import java.util.Collection;
import java.util.HashMap;
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
	
	HashMap<Integer, Vm> vmsTable;		// Id -> Node
	
	public VirtualTopology(int datacenterId, String datacenter){
		this.datacenterId = datacenterId;
		this.datacenter = datacenter;
		vmsTable = new HashMap<Integer, Vm>();
	}
	
	public HashMap<Integer, Vm> getVmsTable() {
		return vmsTable;
	}

	public void addVm(Vm vm){
		vmsTable.put(vm.getId(), vm);
	}
	
	public Vm getVmById(int id){
		return vmsTable.get(id);
	}
	
	public Collection<Vm> getVms(){
		return vmsTable.values();
	}
}
