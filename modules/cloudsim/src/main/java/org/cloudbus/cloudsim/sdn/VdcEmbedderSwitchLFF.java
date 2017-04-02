package org.cloudbus.cloudsim.sdn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.network.datacenter.AggregateSwitch;

public class VdcEmbedderSwitchLFF implements VdcEmbedder {

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
		
		for (VSwitch edgeVSwitch : edgeVSwitches) {
			Boolean embedded = false;
			for (EdgeSwitch edgeSwitch : edgeSwitches) {
				if (embedEdge(edgeVSwitch, edgeSwitch)) {
					embedded = true;
					vswitchMap.put(edgeVSwitch, edgeSwitch);
				}
			}
			if (embedded == false) {
				success = false;
			}
		}
		
		if (success) {
			// embed aggregationswitches
			for (VSwitch aggregationVSwitch : aggregationVSwitches) {
				Boolean embedded = false;
				for (AggregationSwitch aggregationSwitch : aggregationSwitches) {
					if (embedAggregation(aggregationVSwitch, aggregationSwitch, vswitchMap)) {
						embedded = true;
						vswitchMap.put(aggregationVSwitch, aggregationSwitch);
						
						for (VSwitch edgeVSwitch : aggregationVSwitch.getEdgeVSwitches()) {
							Arc vlink = virtualTopology.getVlink(edgeVSwitch, aggregationVSwitch);
							Link link = physicalTopology.getLink(vswitchMap.get(edgeVSwitch).getAddress(), aggregationSwitch.getAddress());
							vlinkMap.put(vlink, link);
						}
					}
				}
				if (embedded == false) {
					success = false;
				}
			}
			
		}
		
		if (success) {
			// embed core
			for (VSwitch coreVSwitch : coreVSwitches) {
				Boolean embedded = false;
				for (CoreSwitch coreSwitch : coreSwitches) {
					if (embedCore(coreVSwitch, coreSwitch, vswitchMap)) {
						embedded = true;
						vswitchMap.put(coreVSwitch, coreSwitch);
						
						for (VSwitch aggregationVSwitch : coreVSwitch.getAggregationVSwitches()) {
							Arc vlink = virtualTopology.getVlink(aggregationVSwitch, coreVSwitch);
							Link link = physicalTopology.getLink(vswitchMap.get(aggregationVSwitch).getAddress(), coreSwitch.getAddress());
							vlinkMap.put(vlink, link);
						}
					}
				}
				if (embedded == false) {
					success = false;
				}
			}
			
		}
		
		if (success) {
			// embed VM
			for (VSwitch edgeVSwitch : edgeVSwitches) {
				for (TimedVm vm : edgeVSwitch.getVMs()) {
					Boolean embedded = false;
					for (SDNHost sdnhost : ((EdgeSwitch)vswitchMap.get(edgeVSwitch)).getSDNHosts()) {
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
		
		
		
		if (success) {
			VdcEmbedding embedding = new VdcEmbedding(vmMap, vswitchMap, vlinkMap);
			return embedding;
		}
		
		// TODO print VDC id
		System.err.println("Embedding Failed.");
		return null;
	}

	private boolean embedEdge(VSwitch edgeVSwitch, EdgeSwitch edgeSwitch) {
		return embedSwitch(edgeVSwitch, (Switch) edgeSwitch);
	}

	private boolean embedSwitch(VSwitch vSwitch, Switch pSwitch) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean embedAggregation(VSwitch aggregationVSwitch, AggregationSwitch aggregationSwitch,
			Map<VSwitch, Switch> vswitchMap) {
		boolean canEmbed = embedSwitch(aggregationVSwitch, aggregationSwitch);
		if (canEmbed) {
			for (VSwitch edgeVSwitch : aggregationVSwitch.getEdgeVSwitches()) {
				EdgeSwitch edgeSwitch = (EdgeSwitch)vswitchMap.get(edgeVSwitch);
				if (!linkExists(edgeSwitch, aggregationSwitch)) {
					canEmbed = false;
				}
				if (!canEmbed) {
					break;
				}
			}
		}
		
		return canEmbed;
	}
	
	private boolean embedCore(VSwitch coreVSwitch, CoreSwitch coreSwitch, Map<VSwitch, Switch> vswitchMap) {
		boolean canEmbed = embedSwitch(coreVSwitch, coreSwitch);
		if (canEmbed) {
			for (VSwitch aggregationVSwitch : coreVSwitch.getAggregationVSwitches()) {
				AggregationSwitch aggregationSwitch = (AggregationSwitch)vswitchMap.get(aggregationVSwitch);
				if (!linkExists(aggregationSwitch, coreSwitch)) {
					canEmbed = false;
				}
				if (!canEmbed) {
					break;
				}
			}
		}
		
		return canEmbed;
	}

	private boolean embedVM(TimedVm vm, SDNHost sdnhost) {
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
		
		host.getRamProvisioner().deallocateRamForVm(vm);
		host.getBwProvisioner().deallocateBwForVm(vm);
		host.getVmScheduler().deallocatePesForVm(vm);
		
		return false;
	}
	
	@Override
	public void rollbackEmbedding(PhysicalTopology physicalTopology, VdcEmbedding embedding) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deallocateVm(PhysicalTopology topology, TimedVm tvm) {
		// TODO Auto-generated method stub
		
	}


}
