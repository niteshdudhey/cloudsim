package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
		
		System.out.println(virtualTopology);
		
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
		
		Boolean success = false;
		
		//embed edgeswitches
		
		int iteration = 0;
		
		
		
		while(!success && iteration < 10) {
		System.out.println("Embedding Iteration: "+iteration);
		success = true;
		
		for (Switch pswitch : switches) {
			resourceMap.put(pswitch, new SwitchResources(pswitch));
		}
		
		List<SwitchResources> edgeSwitchResources = new ArrayList<SwitchResources>();
		for (Switch pswitch : edgeSwitches) {
			edgeSwitchResources.add(resourceMap.get(pswitch));
		}
		
		Set<VSwitch> upperVSwitches = new HashSet<VSwitch>();
		
		Random r = new Random(iteration);
		Collections.shuffle(edgeSwitchResources, new Random(r.nextLong()));
		
		
		for (VSwitch edgeVSwitch : edgeVSwitches) {
			Boolean embedded = false;
			for (VNode vnode1 : edgeVSwitch.getUpperVNodes()) {
				VSwitch vswitch = (VSwitch) vnode1;
				upperVSwitches.add(vswitch);
			}
			for (SwitchResources switchResources : edgeSwitchResources) {
//				System.out.println(edgeVSwitch.getId()+" "+switchResources.getSwitch().getId());
				if (embedEdge(edgeVSwitch, switchResources)) {
					System.out.println("Mapped vswitch #" + edgeVSwitch.getId()+" to switch #"+switchResources.getSwitch().getId());
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
			
			for (VNode vnode : upperVSwitches) {
				VSwitch internalVSwitch = (VSwitch) vnode;
				for (VNode vnode1 : internalVSwitch.getUpperVNodes()) {
					VSwitch vswitch = (VSwitch) vnode1;
					newUpperVSwitches.add(vswitch);
				}
				Boolean embedded = false;
				List<SwitchResources> upperSwitchResourcesList = new ArrayList<SwitchResources>();
				upperSwitchResourcesList.addAll(upperSwitchResources);
				Collections.shuffle(upperSwitchResourcesList,  new Random(r.nextLong()));
				for (SwitchResources switchResources : upperSwitchResourcesList) {
					if (embedInternal(internalVSwitch, switchResources, vswitchMap, physicalTopology)) {
						System.out.println("Mapped vswitch #" + internalVSwitch.getId()+" to switch #"+switchResources.getSwitch().getId());
						
						embedded = true;
						vswitchMap.put(internalVSwitch, switchResources.getSwitch());
						
						for (VNode vnode2 : internalVSwitch.getLowerVNodes()) {
							VSwitch lowerVSwitch = (VSwitch) vnode2;
							Arc vlink = virtualTopology.getVlink(internalVSwitch.getId(), lowerVSwitch.getId());
							Link link = physicalTopology.getLink(vswitchMap.get(lowerVSwitch).getAddress(), switchResources.getSwitch().getAddress());
							List<Link> linklist = new ArrayList<Link>();
							linklist.add(link);
							vlinkMap.put(vlink, linklist);
							// link embedding to be done
						}
						break;
					}
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
				for (VNode vnode : edgeVSwitch.getLowerVNodes()) {
					Vm vm = (Vm) vnode;
					Boolean embedded = false;
					List<Node> hosts = ((EdgeSwitch)vswitchMap.get(edgeVSwitch)).getLowerNodes();
					Collections.shuffle(hosts, new Random(r.nextLong()));
					for (Node node : hosts) {
						SDNHost sdnhost = (SDNHost) node;
						if (embedVM(vm, sdnhost)) {
							System.out.println("Mapped vm #" + vm.getId()+" to sdnhost #"+sdnhost.getId());
							
							embedded = true;
							vmMap.put(vm, sdnhost);
							Arc vlink = virtualTopology.getVlink(vm.getId(), edgeVSwitch.getId());
							Link link = physicalTopology.getLink(vswitchMap.get(edgeVSwitch).getAddress(), sdnhost.getAddress());
							List<Link> linklist = new ArrayList<Link>();
							linklist.add(link);
							vlinkMap.put(vlink, linklist);
							break;
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
		
		
		iteration++;
		}

		
		
		
		if (success) {
			System.out.println("Embedding Success.");
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
		boolean flag = true;

		if (switchResources.getCurrentBandwidth() < vSwitch.getBw()) {
			flag = false;
		}
		if (switchResources.getCurrentIops() < vSwitch.getIops()) {
			flag = false;
		}
		if (flag) {
			switchResources.setCurrentBandwidth(switchResources.getCurrentBandwidth() - vSwitch.getBw());
			switchResources.setCurrentIops(switchResources.getCurrentIops() - vSwitch.getIops()); 
		}
		return flag;
	}

	private boolean embedInternal(VSwitch internalVSwitch, SwitchResources switchResources,
			Map<VSwitch, Switch> vswitchMap, PhysicalTopology physicalTopology) {
		boolean canEmbed = embedSwitch(internalVSwitch, switchResources);
		if (canEmbed) {
			for (VNode vnode : internalVSwitch.getLowerVNodes()) {
				VSwitch lowerVSwitch = (VSwitch) vnode;
				Switch lowerSwitch = vswitchMap.get(lowerVSwitch);
				
				if (!physicalTopology.linkExists(lowerSwitch.getAddress(), switchResources.getSwitch().getAddress())) {
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

		System.out.println(vm.getId());
		System.out.println(sdnhost.getName());
		return true;
	}
	
	private void unembedVM(Vm vm, SDNHost sdnhost) {
		
		Host host = sdnhost.getHost();
		
		host.vmDestroy(vm);		
	}
	
	private List<SwitchResources> getResources(List<Node> nodes) {
		List<SwitchResources> resources = new ArrayList<SwitchResources>();
		for (Node node : nodes) {
			Switch pswitch = (Switch) node;
			resources.add(resourceMap.get(pswitch));
		}
		return resources;
	}
	
	
	@Override
	public void rollbackEmbedding(PhysicalTopology physicalTopology, VdcEmbedding embedding) {
		// TODO Auto-generated method stub
		Map<Vm, SDNHost> vmMap = embedding.getVmMap();
		Map<VSwitch, Switch> vswitchMap = embedding.getVSwitchMap();
		Map<Arc, List<Link>> vlinkMap = embedding.getVLinkMap();
		
		for (Map.Entry<Vm, SDNHost> entry : vmMap.entrySet())
		{
		    unembedVM(entry.getKey(), entry.getValue());
		}
		
		for (Map.Entry<VSwitch, Switch> entry : vswitchMap.entrySet())
		{
		    entry.getValue().vswitchDestroy(entry.getKey());
		}
		
		// link to be un embedded
		
		
	}

	@Override
	public void deallocateVm(PhysicalTopology topology, TimedVm tvm) {
		// TODO Auto-generated method stub
		
	}
	
	private class SwitchResources {

		private Switch pswitch;
		
		private int currentBw;
		
		private Long currentIops;
		
		public SwitchResources(Switch pswitch) {
			this.pswitch = pswitch;
			this.currentBw = pswitch.getCurrentBandwidth();
			this.currentIops = pswitch.getCurrentIops();
		}

		public Switch getSwitch() {
			return pswitch;
		}
		
		public int getCurrentBandwidth() {
			return currentBw;
		}
		
		public long getCurrentIops() {
			return currentIops;
		}
		
		public void setCurrentBandwidth(int bw) {
			currentBw = bw;
		}
		
		public void setCurrentIops(Long iops) {
			currentIops = iops;
		}
	
	}
}

