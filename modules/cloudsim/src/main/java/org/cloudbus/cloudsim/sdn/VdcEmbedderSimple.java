/**
 * 
 */
package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.List;

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
		
	@Override
	public VdcEmbedding embed(PhysicalTopology physicalTopology, VirtualTopology virtualTopology) {
		
		System.out.println("In Embed function");
		// The embedding to be returned.
		VdcEmbedding embedding = new VdcEmbedding();
		
		List<Integer> freePes = new ArrayList<Integer>();
		
		for(SDNHost host : physicalTopology.getHostList()){
			freePes.add(host.getHost().getNumberOfFreePes());
		}
		
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

				Host host = physicalTopology.getHostList().get(idx).getHost();
				
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
					
					host.getRamProvisioner().deallocateRamForVm(vm);
					
					result = false;
				}

				if (!host.getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
					System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() +
							" failed by MIPS");
					
					host.getRamProvisioner().deallocateRamForVm(vm);
					host.getBwProvisioner().deallocateBwForVm(vm);
					
					result = false;
				}
				
				// If VM were successfully created in the host.
				if (result) {
					freePes.set(idx, freePes.get(idx) - requiredPes);
					
					System.out.println("Allocating vm:" + vm.getId() + " to host:" + host.getId());
					
					SDNHost sdnHost = physicalTopology.getHostList().get(idx);
					embedding.allocateVmToHost(vm.getId(), sdnHost.getId());
					
					break;
				}
				else {
					freePesTmp.set(idx, Integer.MIN_VALUE);
				}
				
				tries++;
				
			} while (!result && tries < freePes.size());
		}
		
		System.out.println("Embedding is as follows:");
		System.out.println(embedding.toString());
		
		return embedding;
	}
}
