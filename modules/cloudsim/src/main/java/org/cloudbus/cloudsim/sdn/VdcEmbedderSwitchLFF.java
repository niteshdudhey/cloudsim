package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.network.datacenter.AggregateSwitch;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

public class VdcEmbedderSwitchLFF implements VdcEmbedder {
	
	private Map<Switch, SwitchResources> resourceMap;

	@Override
	public void init(PhysicalTopology topology) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VdcEmbedding embed(PhysicalTopology physicalTopology, VirtualTopology virtualTopology) {
		// TODO Auto-generated method stub
		List<SDNHost> sdnhosts = physicalTopology.getHostList();
		List<EdgeSwitch> edgeSwitches = physicalTopology.getEdgeSwitchList();
		List<AggregationSwitch> aggregationSwitches = physicalTopology.getAggregationSwitchList();
		List<CoreSwitch> coreSwitches = physicalTopology.getCoreSwitchList();
		
		List<Switch> switches = physicalTopology.getSwitchList();
		resourceMap = new HashMap<Switch, SwitchResources>();
		
		for (Switch pswitch : switches) {
			resourceMap.put(pswitch, new SwitchResources(pswitch));
		}
		
		List<Vm> vms = virtualTopology.getVmList();
		List<VSwitch> edgeVSwitches = virtualTopology.getEdgeVSwitchList();
		List<VSwitch> aggregationVSwitches = virtualTopology.getAggregationVSwitchList();
		List<VSwitch> coreVSwitches = virtualTopology.getCoreVSwitchList();
		
		Map<Vm, SDNHost> vmMap;
		Map<VSwitch, Switch> vswitchMap;
		Map<Arc, List<Link>> vlinkMap;
		
		vmMap = new HashMap<Vm, SDNHost>();
		vswitchMap = new HashMap<VSwitch, Switch>();
		vlinkMap = new HashMap<Arc, List<Link>>();
		
		// TODO sorting to be handled
		
		Boolean success = true;
		
		//embed edgeswitches
		
		Set<VSwitch> upperVSwitches = new HashSet<VSwitch>();
		
		List<SwitchResources> edgeSwitchResources = new ArrayList<SwitchResources>();
		for (Switch pswitch : edgeSwitches) {
			edgeSwitchResources.add(resourceMap.get(pswitch));
		}
		
		for (VSwitch edgeVSwitch : edgeVSwitches) {
			Boolean embedded = false;
			upperVSwitches.addAll(edgeVSwitch.getUpperVNodes());
			for (SwitchResources switchResources : edgeSwitchResources) {
				if (embedEdge(edgeVSwitch, switchResources)) {
					embedded = true;
					vswitchMap.put(edgeVSwitch, switchResources.getSwitch());
					break;
				}
			}
			if (embedded == false) {
				success = false;
				break;
			}
		}
		
		Set<SwitchResources> upperSwitchResources = new HashSet<SwitchResources>();
		for (SwitchResources switchResources : edgeSwitchResources) {
			upperSwitchResources.addAll(getResources(switchResources.getSwitch().getUpperNodes()));
		}
		
		while(success && !upperVSwitches.isEmpty()) {
			// embed other switches
			Set<VSwitch> newUpperVSwitches = new HashSet<VSwitch>();
			Set<SwitchResources> newUpperSwitchResources = new HashSet<SwitchResources>();
			
			for (VSwitch internalVSwitch : upperVSwitches) {
				newUpperVSwitches.addAll(internalVSwitch.getUpperVNodes());
				Boolean embedded = false;
				
				for (SwitchResources switchResources : upperSwitchResources) {
					if (embedInternal(internalVSwitch, switchResources, vswitchMap)) {
						embedded = true;
						vswitchMap.put(internalVSwitch, switchResources.getSwitch());
						
						for (VSwitch lowerVSwitch : internalVSwitch.getLowerVNodes()) {
							Arc vlink = virtualTopology.getVlink(lowerVSwitch.getId(), internalVSwitch.getId());
							Link link = physicalTopology.getLink(vswitchMap.get(lowerVSwitch).getAddress(), switchResources.getSwitch().getAddress());
							vlinkMap.put(vlink, link);
						}
					}
					break;
				}
				if (embedded == false) {
					success = false;
					break;
				}
			}
			upperVSwitches = newUpperVSwitches;
			
			for (SwitchResources switchResources : upperSwitchResources) {
				newUpperSwitchResources.addAll(getResources(switchResources.getSwitch().getUpperNodes()));
			}
			
			upperSwitchResources = newUpperSwitchResources;
			
		}
		
		
		
		
		if (success) {
			// embed VM
			for (VSwitch edgeVSwitch : edgeVSwitches) {
				for (Vm vm : edgeVSwitch.getLowerVNodes()) {
					Boolean embedded = false;
					for (SDNHost sdnhost : ((EdgeSwitch)vswitchMap.get(edgeVSwitch)).getLowerNodes()) {
						if (embedVM(vm, sdnhost)) {
							embedded = true;
							vmMap.put(vm, sdnhost);
						}
					}
					if (embedded == false) {
						success = false;
					}
				}
			}
					
		}
		

		for (Map.Entry<Vm, SDNHost> entry : vmMap.entrySet()) {
			unembedVM(entry.getKey(), entry.getValue());
		}

		
		
		
		if (success) {
			VdcEmbedding embedding = new VdcEmbedding(vmMap, vswitchMap, vlinkMap);
			return embedding;
		}
		
		// TODO print VDC id
		System.err.println("Embedding Failed.");
		return null;
	}



	private boolean embedEdge(VSwitch edgeVSwitch, SwitchResources switchResources) {
		return embedSwitch(edgeVSwitch, switchResources);
	}

	private boolean embedSwitch(VSwitch vSwitch, SwitchResources switchResources) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean embedInternal(VSwitch internalVSwitch, SwitchResources switchResources,
			Map<VSwitch, Switch> vswitchMap) {
		boolean canEmbed = embedSwitch(internalVSwitch, switchResources);
		if (canEmbed) {
			for (VSwitch lowerVSwitch : internalVSwitch.getLowerVNodes()) {
				Switch lowerSwitch = vswitchMap.get(lowerVSwitch);
				if (!linkExists(lowerSwitch.getAddress(), switchResources.getSwitch().getAddress())) {
					canEmbed = false;
				}
				if (!canEmbed) {
					break;
				}
			}
		}
		
		return canEmbed;
	}


	private boolean embedVM(Vm vm, SDNHost sdnhost) {
		// TODO Auto-generated method stub
		
		Host host = sdnhost.getHost();
		
		if (host.getStorage() < vm.getSize()) {
			System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() + 
					" failed by storage");
			return false;
		}

		if (!host.getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
			System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() +
					" failed by RAM");
			
			return false;
		}
		

		if (!host.getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
			System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() +
					" failed by BW");
			
			host.getRamProvisioner().deallocateRamForVm(vm);
			return false;
		}

		if (!host.getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			System.out.println("Allocation of VM #" + vm.getId() + " to Host #" + host.getId() +
					" failed by MIPS");
			
			host.getRamProvisioner().deallocateRamForVm(vm);
			host.getBwProvisioner().deallocateBwForVm(vm);
			return false;
		}
		
		host.setStorage(host.getStorage() - vm.getSize());
		
//		host.getRamProvisioner().deallocateRamForVm(vm);
//		host.getBwProvisioner().deallocateBwForVm(vm);
//		host.getVmScheduler().deallocatePesForVm(vm);
		
		return true;
	}
	
	private void unembedVM(Vm vm, SDNHost sdnhost) {
		
		Host host = sdnhost.getHost();
		
		host.setStorage(host.getStorage() + vm.getSize());
		
		host.getRamProvisioner().deallocateRamForVm(vm);
		host.getBwProvisioner().deallocateBwForVm(vm);
		host.getVmScheduler().deallocatePesForVm(vm);		
	}
	
	private List<SwitchResources> getResources(List<Switch> switches) {
		List<SwitchResources> resources = new ArrayList<SwitchResources>();
		for (Switch pswitch : switches) {
			resources.add(resourceMap.get(pswitch));
		}
		return resources;
	}
	
	
	@Override
	public void rollbackEmbedding(PhysicalTopology physicalTopology, VdcEmbedding embedding) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deallocateVm(PhysicalTopology topology, TimedVm tvm) {
		// TODO Auto-generated method stub
		
	}
	
	private class SwitchResources {
		
		private Switch pswitch;
		
		public SwitchResources(Switch pswitch) {
			this.pswitch = pswitch;
		}

		public Switch getSwitch() {
			return pswitch;
		}
		
		

		
	}


}

