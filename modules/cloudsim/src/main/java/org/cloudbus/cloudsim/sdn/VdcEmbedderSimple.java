/**
 * 
 */
package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

/**
 * The embedding policy which tries to allocate each VM to a host untill it finds some host to allocate.
 * 
 * @author Nitesh Dudhey
 *
 */

public class VdcEmbedderSimple implements VdcEmbedder {
	
	/** The map between each VM and its allocated host.
     * The map key is a VM UID and the value is the allocated host for that VM. */
	private Map<String, SDNHost> vmTable;

	/** The map between each VM and the number of Pes used. 
 	* The map key is a VM UID and the value is the number of used Pes for that VM. */
	private Map<String, Integer> usedPes;

	/** The number of free Pes for each host from {@link #getHostList() }. */
	private List<Integer> freePes;

	@Override
	public void init(PhysicalTopology topology){
		
		setFreePes(new ArrayList<Integer>());
		
		for (SDNHost sdnHost : topology.getHostList()) {
			getFreePes().add(sdnHost.getHost().getNumberOfPes());
		}

		setVmTable(new HashMap<String, SDNHost>());
		setUsedPes(new HashMap<String, Integer>());
	}

	// TODO: Handle case when VDC cannot be embedded.
	@Override
	public VdcEmbedding embed(PhysicalTopology physicalTopology, VirtualTopology virtualTopology) {
		
		// The embedding to be returned.
		VdcEmbedding embedding = new VdcEmbedding();
		
		/*List<Integer> freePes = new ArrayList<Integer>();
		
		for(SDNHost host : physicalTopology.getHostList()){
			freePes.add(host.getHost().getNumberOfFreePes());
		}*/
		
		// Setting embeddings for VMs.
		for(Vm vm : virtualTopology.getVms()){
			System.out.println("Finding host for VM:" + vm.getId());
			
			List<Integer> freePesTmp = new ArrayList<Integer>();
			
			for (Integer numberOfFreePes : freePes) {
				freePesTmp.add(numberOfFreePes);
			}
			
			int requiredPes = vm.getNumberOfPes();
			boolean result = true;
			int tries = 0;
			
			do {
				// We try until we find a host or until we try all of them.
				
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				// We want the host with less pes in use.
				for (int i = 0 ; i < freePesTmp.size() ; i++) {
					if (moreFree < freePesTmp.get(i)) {
						moreFree = freePesTmp.get(i);
						idx = i;
					}
				}

				SDNHost sdnHost = physicalTopology.getHostList().get(idx);
				Host host = sdnHost.getHost();
				
				if (host.getStorage() < vm.getSize()) {
					System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() + 
							" failed by storage");
					result = false;
				}

				if (!host.getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
					System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() +
							" failed by RAM");
					
					result = false;
				}

				if (!host.getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
					System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() +
							" failed by BW");
					
					result = false;
				}

				if (!host.getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
					System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() +
							" failed by MIPS");
					
					result = false;
				}
				
				// If VM were successfully created in the host.
				if (result) {
					
					getVmTable().put(vm.getUid(), sdnHost);
					getUsedPes().put(vm.getUid(), requiredPes);
					getFreePes().set(idx, freePes.get(idx) - requiredPes);
					
					System.out.println("Allocating vm:" + vm.getId() + " to host:" + host.getId());
					
					embedding.allocateVmToHost(vm.getId(), sdnHost.getId());
					
					break;
				}
				else {
					freePesTmp.set(idx, Integer.MIN_VALUE);
				}
				
				tries++;
				
			} while (!result && tries < freePes.size());
		}
		
		//System.out.println("Embedding is as follows:");
		//System.out.println(embedding.toString());
		
		return embedding;
	}
	
	@Override
	public void deallocateVm(PhysicalTopology topology, TimedVm tvm){

		SDNHost sdnHost = getVmTable().remove(tvm.getUid());
		
		int idx = topology.getHostList().indexOf(sdnHost);
		int pes = getUsedPes().remove(tvm.getUid());
		
		if (sdnHost != null) {
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}
	}
	
	protected Map<String, SDNHost> getVmTable() {
		return vmTable;
	}

	protected void setVmTable(Map<String, SDNHost> vmTable) {
		this.vmTable = vmTable;
	}

	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	protected List<Integer> getFreePes() {
		return freePes;
	}

	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}
}
