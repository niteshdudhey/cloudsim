/**
 * 
 */
package org.cloudbus.cloudsim.sdn;

import java.util.HashMap;

/**
 * The class to represent a VDC embedding on a Physical Datacenter.
 * An object of this will be returned when the VDCEPolicy will be asked to find the embedding for the VDC.
 *  
 * @author Nitesh Dudhey
 *
 */
public class VdcEmbedding {
	// Maps VM id to SDNHost id on which it is assigned.
	HashMap<Integer, Integer> vmToHostMappings;
	
	public VdcEmbedding() {
		vmToHostMappings = new HashMap<Integer, Integer>();
	}
	
	public HashMap<Integer, Integer> getVmToHostMappings() {
		return vmToHostMappings;
	}

	public int getAllocatedHostForVm(int id){
		return vmToHostMappings.get(id);
	}
	
	public void allocateVmToHost(int vmId, int hostId){
		vmToHostMappings.put(vmId, hostId);
	}
	
	public String toString(){
		StringBuilder ret = new StringBuilder();
		
		ret.append("VmToHostMappings\n");
		ret.append(vmToHostMappings.toString());
		
		return ret.toString();
	}
}
