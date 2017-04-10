/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.sdn.example;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.sdn.Arc;
import org.cloudbus.cloudsim.sdn.Link;
import org.cloudbus.cloudsim.sdn.Middlebox;
import org.cloudbus.cloudsim.sdn.NetworkOperatingSystem;
import org.cloudbus.cloudsim.sdn.Node;
import org.cloudbus.cloudsim.sdn.SDNDatacenter;
import org.cloudbus.cloudsim.sdn.SDNHost;
import org.cloudbus.cloudsim.sdn.Switch;
import org.cloudbus.cloudsim.sdn.TimedVm;
import org.cloudbus.cloudsim.sdn.VSwitch;
import org.cloudbus.cloudsim.sdn.VdcEmbedder;
import org.cloudbus.cloudsim.sdn.VdcEmbedding;
import org.cloudbus.cloudsim.sdn.VirtualTopology;

/**
 * Simple network operating system class for the example. 
 * In this example, network operating system (aka SDN controller) finds shortest path
 * when deploying the application onto the cloud. 
 * 
 * @author Jungmin Son
 * @since CloudSimSDN 1.0
 */
public class SimpleNetworkOperatingSystem extends NetworkOperatingSystem {

	public SimpleNetworkOperatingSystem(String fileName, VdcEmbedder embedder) {
		super(fileName, embedder);
	}
	
	@Override
	protected boolean deployApplication(List<Vm> vms, List<Middlebox> middleboxes, List<Arc> links,
			List<VSwitch> vswitchList) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Starting deploying application..");
		
		for(Vm vm : vms) {	
			TimedVm tvm = (TimedVm) vm;
			SDNDatacenter datacenter = getDatacenterById(tvm.getDatacenterId());
			
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId() 
					+ " in " + datacenter.getName() + ", (" + tvm.getStartTime() + "~" + tvm.getFinishTime() + ")");
			
			send(datacenter.getId(), tvm.getStartTime(), CloudSimTags.VM_CREATE_ACK, vm);
			
			if(tvm.getFinishTime() != Double.POSITIVE_INFINITY) {
				send(datacenter.getId(), tvm.getFinishTime(), CloudSimTags.VM_DESTROY, vm);
				send(this.getId(), tvm.getFinishTime(), CloudSimTags.VM_DESTROY, vm);
			}
		}
		for (VSwitch vswitch: vswitchList) {
			SDNDatacenter datacenter = getDatacenterById(vswitch.getDatacenterId());
			Log.printLine(CloudSim.clock() + ": " + "Trying to Create VSwitch " + vswitch.getName() 
			+ " in " + datacenter.getName() + ", (" + vswitch.getStartTime() + "~" + vswitch.getFinishTime() + ")");
	
			send(datacenter.getId(), vswitch.getStartTime(), CloudSimTags.VSWITCH_CREATE_ACK, vswitch);
			
			if(vswitch.getFinishTime() != Double.POSITIVE_INFINITY) {
				send(datacenter.getId(), vswitch.getFinishTime(), CloudSimTags.VSWITCH_DESTROY, vswitch);
				send(this.getId(), vswitch.getFinishTime(), CloudSimTags.VSWITCH_DESTROY, vswitch);
			}
		}
		return true;
	}
	
	// @deprecated.
	@Override
	public boolean deployApplication(List<Vm> vms, List<Middlebox> middleboxes, List<Arc> links, VirtualTopology virtualTopology) {
		return false;
	}
	
	@Override
	public boolean deployApplication(VirtualTopology virtualTopology) {
		
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Starting deploying application..");
		
		VdcEmbedding embedding = embedder.embed(topology, virtualTopology);
		
		vdcEmbeddingMap.put(virtualTopology, embedding);
		
		System.out.println("embedding: " + embedding);
		
		if(!isValidEmbedding(embedding, virtualTopology)){
			System.out.println("Embedding Failed!!!");
			return false;
		}
		
		Map<Integer, Vm> vms1 = virtualTopology.getVmsTable();
		
		for (Entry<Integer, Vm> entry : vms1.entrySet()) {
			Vm vm = entry.getValue();
			
			SDNHost sdnHost = embedding.getAllocatedHostForVm(vm);
			
			TimedVm tvm = (TimedVm) vm;
			
			tvm.setCandidateHost(sdnHost);
			
			SDNDatacenter datacenter = getDatacenterById(tvm.getDatacenterId());
			
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + tvm.getId() 
					+ " in " + datacenter.getName() + ", (" + tvm.getStartTime() + "~" + tvm.getFinishTime() + ")");
			
			send(datacenter.getId(), tvm.getStartTime(), CloudSimTags.VM_CREATE_ACK, tvm);
			
			if(tvm.getFinishTime() != Double.POSITIVE_INFINITY) {
				send(datacenter.getId(), tvm.getFinishTime(), CloudSimTags.VM_DESTROY, tvm);
				send(this.getId(), tvm.getFinishTime(), CloudSimTags.VM_DESTROY, tvm);
			}
		}
		
		Map<Integer, VSwitch> vswitches = virtualTopology.getVSwitchesTable();
		
		for (Entry<Integer, VSwitch> entry : vswitches.entrySet()) {
			VSwitch vswitch = entry.getValue();
			
			Switch pswitch = embedding.getAllocatedSwitchForVSwitch(vswitch);
					
			vswitch.setSwitch(pswitch);
			
			SDNDatacenter datacenter = getDatacenterById(vswitch.getDatacenterId());
			
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VSwitch #" + vswitch.getId() 
					+ " in " + datacenter.getName() + ", (" + vswitch.getStartTime() + "~" + vswitch.getFinishTime() + ")");
			
			send(datacenter.getId(), vswitch.getStartTime(), CloudSimTags.VSWITCH_CREATE_ACK, vswitch);
			
			if(vswitch.getFinishTime() != Double.POSITIVE_INFINITY) {
				send(datacenter.getId(), vswitch.getFinishTime(), CloudSimTags.VSWITCH_DESTROY, vswitch);
				send(this.getId(), vswitch.getFinishTime(), CloudSimTags.VSWITCH_DESTROY, vswitch);
			}
		}
		
		return true;
	}
	
	/**
	 * Returns true if each VM is allocated to some host.
	 * 
	 * @param embedding
	 * @param virtualTopology
	 * @return
	 */
	private boolean isValidEmbedding(VdcEmbedding embedding, VirtualTopology virtualTopology){
		if (embedding.getVmMap().size() == virtualTopology.getVms().size()) {
			return true;
		}
		return false;
	}
	
	public boolean deployFlow(List<Arc> links) {
		
		for(Arc link : links) {
			int srcVm = link.getSrcId();
			int dstVm = link.getDstId();
			int flowId = link.getFlowId();
			
			SDNHost srchost = findSDNHost(srcVm);
			SDNHost dsthost = findSDNHost(dstVm);
			if(srchost == null || dsthost == null) {
				continue;
			}
			
			if(srchost.equals(dsthost)) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Source SDN Host is same as Destination. Go loopback");
				
				srchost.addVMRoute(srcVm, dstVm, flowId, dsthost);
			}
			else {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": VMs are in different hosts. Create entire routing table (hosts, switches)");
				
				boolean findRoute = buildForwardingTables(srchost, srcVm, dstVm, flowId, null);
				
				if(!findRoute) {
					System.err.println("SimpleNetworkOperatingSystem.deployFlow: Could not find route!!" 
							+ NetworkOperatingSystem.debugVmIdName.get(srcVm) + "->" 
							+ NetworkOperatingSystem.debugVmIdName.get(dstVm));
				}
			}
		}
		
		// Print all routing tables.
		for(Node node : this.topology.getAllNodes()) {
			node.printVMRoute();
		}
		
		return true;
	}
	
	public boolean deployFlowVms(List<Arc> links) {
		System.out.println("Reached deployFlowVms()");
		for(Arc link : links) {
			int src = link.getSrcId();
			int dst = link.getDstId();
			int flowId = link.getFlowId();
			
			System.out.println(src + " " + dst + " " + flowId);
			
			SDNHost srchost = findSDNHost(src);
			SDNHost dsthost = findSDNHost(dst);
			
			Switch srcSwitch = findSwitch(src);
			Switch dstSwitch = findSwitch(dst);
			
			System.out.println(srchost);
			System.out.println(dsthost);
			System.out.println(srcSwitch);
			System.out.println(dstSwitch);
			
			if (srchost == null && srcSwitch == null) {
				continue;
			}
			
			if (dsthost == null && dstSwitch == null) {
				continue;
			}
						
			if(srchost != null && dsthost != null && srchost.equals(dsthost)) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Source SDN Host is same as Destination. Go loopback");
				
				srchost.addVMRoute(src, dst, flowId, dsthost);
			}
			else {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": VMs are in different hosts. Create entire routing table (hosts, switches)");
				
				boolean findRoute = false;
				if (srchost != null) {
					findRoute = buildNewForwardingTables(srchost, src, dst, flowId, null);
				} else {
					findRoute = buildNewForwardingTables(srcSwitch, src, dst, flowId, null);
				}
				
				if(!findRoute) {
					System.err.println("SimpleNetworkOperatingSystem.deployFlow: Could not find route!!" 
							+ NetworkOperatingSystem.debugVmIdName.get(src) + "->" 
							+ NetworkOperatingSystem.debugVmIdName.get(dst));
				}
			}
		}
		
		// Print all routing tables.
		for(Node node : this.topology.getAllNodes()) {
			node.printVMRoute();
		}
		
		return true;
	}
	
	private Link selectLinkFirst(List<Link> links) {
		return links.get(0);
	}
	
	int i = 0;
	private Link selectLinkRandom(List<Link> links) {
		return links.get(i++ % links.size());
	}

	private Link selectLinkByFlow(List<Link> links, int flowId) {
		if(flowId == -1) {
			return links.get(0);
		}
		else {
			return links.get(1 % links.size());
		}
	}
	
	private Link selectLinkByChannelCount(Node from, List<Link> links) {
		Link lighter = links.get(0);
		
		for(Link l : links) {
			if(l.getChannelCount(from) < lighter.getChannelCount(from)) {
				// Less traffic flows using this link.
				lighter = l; 
			}
		}
		return lighter;
	}

	private Link selectLinkByDestination(List<Link> links, SDNHost destHost) {
		int numLinks = links.size();
		int linkid = destHost.getAddress() % numLinks;
		Link link = links.get(linkid);
		return link;
	}
	
	private void createNewFlows() {
		int flowId, srcId, dstId;
		for (Vm vm1: vmList) {
			for (Vm vm2: vmList) {
				srcId = vm1.getId();
				dstId = vm2.getId();
				if (checkFlowExists(srcId, dstId)) {
					continue;
				}
				SDNHost srcHost = findSDNHost(srcId);
				SDNHost dstHost = findSDNHost(dstId);
				if (srcHost == null || dstHost == null) {
					continue;
				}
				System.out.println("Creating flow b/w " + srcId + " " + dstId);
				flowNumbers++;
				flowId = flowNumbers;
				if (srcHost.equals(dstHost)) {
					srcHost.addVMRoute(srcId, dstId, flowId, dstHost);
				} else {
					buildForwardingTables(srcHost, srcId, dstId, flowId, null);
				}
				System.out.println("New flow with FlowId = " + flowId + " between " + srcId + " " + dstId + " created");
				updateVSwitchesInFlow(srcHost, srcId, dstId, flowId, 1000);
				Arc arc = new Arc(Integer.toString(flowId), srcId, dstId, flowId, 0, 0);
				newFlows.add(arc);
				this.flowIdArcTable.put(flowId, arc);
			}
		}
	}
	
	private void updateVSwitchesInFlow(Node node, int srcId, int dstId, int flowId, int pathLength) {
		if (pathLength == 0) {
			return;
		}
		if (getFlowIdVSwitchListTable().get(flowId) == null) {
			getFlowIdVSwitchListTable().put(flowId, new LinkedList<VSwitch>());
		}
		Node next = node.getVMRoute(srcId, dstId, flowId);
		System.out.println("VSwitches in Flow: " + next);
		if (next instanceof Switch) {
			if (!((Switch)next).getVSwitchList().isEmpty()) {
				VSwitch vswitch = ((Switch) next).getVSwitchList().get(0);
				getFlowIdVSwitchListTable().get(flowId).add(vswitch);
			}
		}
		updateVSwitchesInFlow(next, srcId, dstId, flowId, pathLength-1);
	}
	
	private boolean checkFlowExists(int srcId, int dstId) {
		for (Arc arc: newFlows) {
			if (arc.getSrcId() == srcId && arc.getDstId() == dstId) {
				return true;
			}
		}
		for (Arc arc: arcList) {
			if (arc.getSrcId() == srcId && arc.getDstId() == dstId) {
				return true;
			}
		}
		return false;
	}

	private boolean buildNewForwardingTables(Node node, int src, int dst, int flowId, Node prevNode) {
		SDNHost destHost = findSDNHost(dst);
		Switch destSwitch = findSwitch(dst);
		
		Node dest = null;
		if (destHost == null) {
			dest = destSwitch;
		} else {
			dest = destHost;
		}
		
		if (node.equals(dest)) {
			return true;
		}
		
		List<Link> nextLinks = node.getRoute(dest);
		
		System.out.println(node.toString());
		System.out.println(dest.toString());
		Link nextLink = selectLinkByFlow(nextLinks, flowId);
		Node nextHop = nextLink.getOtherNode(node);
				
		node.addVMRoute(src, dst, flowId, nextHop);
		buildNewForwardingTables(nextHop, src, dst, flowId, null);
		
		return true;
	}
	
	private boolean buildForwardingTables(Node node, int srcVm, int dstVm, int flowId, Node prevNode) {
		// There are many links. Determine which hop to go.
		SDNHost desthost = findSDNHost(dstVm);
		
		if(node.equals(desthost)) {
			return true;
		}
		
		List<Link> nextLinks = node.getRoute(desthost);
		
		// Let's choose the first link. make simple.
		Link nextLink = selectLinkByFlow(nextLinks, flowId);
		//Link nextLink = selectLinkRandom(nextLinks);
		//Link nextLink = selectBestLink(node, nextLinks);
		//Link nextLink = selectRandomTreeLink(nextLinks, desthost);
		Node nextHop = nextLink.getOtherNode(node);
		
		node.addVMRoute(srcVm, dstVm, flowId, nextHop);
		buildForwardingTables(nextHop, srcVm, dstVm, flowId, null);
		
		return true;
			
		/*
		Collection<Link> links = this.topology.getAdjacentLinks(node);
		if(links.size() == 0) {
			// No link. Do nothing
		}
		else if(links.size() == 1) {
			// Only one way, no other choice (for Host to Edge switch)
			for(Link l:links) {
				Node nextHop= l.getHighOrder();
				if(nextHop.equals(node))
					nextHop= l.getLowOrder();
				
				node.addVMRoute(srcVm, dstVm, flowId, nextHop);
				buildForwardingTables(nextHop, srcVm, dstVm, flowId, node);
			}
			return true;
		}
		else {
			// There are many links. Determine which hop to go.
			SDNHost dsthost = findSDNHost(dstVm);
			
			for(Link l:links) {
				Node nextHop= l.getOtherNode(node);
				
				if(nextHop.equals(prevNode)) {
					// NextHop is going back to prev node
					continue;	
				}
				else if(nextHop.equals(dsthost)) {
					// NextHop is the destination. Just add. No further route finding.
					node.addVMRoute(srcVm, dstVm, flowId, nextHop);
					return true;
				} 
				else if(nextHop instanceof SDNHost) {
					// NextHop is host but no destination. Can't forward
					continue;
				}
				else {
					// Nexthop is switch
					if(buildForwardingTables(nextHop, srcVm, dstVm, flowId, node)) {
						// If the route is right.
						node.addVMRoute(srcVm, dstVm, flowId, nextHop);
						return true;
					}
					else
						continue;
				}
			}
		}
		return false;
		*/
	}

	@Override
	protected Middlebox deployMiddlebox(String type, Vm vm) {
		return null;
	}
	
	@Override
	public void processVmCreateAck(SimEvent ev) {
		// Print the created VM info.
		TimedVm vm = (TimedVm) ev.getData();
		SDNHost host = this.findSDNHost(vm.getId());
		
		Log.printLine(CloudSim.clock() + ": " + getName() 
				+ ": VM Created: " +  vm.getId() + " in " + host);
		
//		deployFlowVms(this.arcList);
//		createNewFlows();
		
		// Can optimize by calling createFlowsForVm(vm, ..).
		createFlowsBetweenVms();
		
	}
	
	@Override
	public void processVSwitchCreateAck(SimEvent ev) {
		VSwitch vswitch = (VSwitch) ev.getData();
		Switch pswitch = vswitch.getSwitch();
		
		Log.printLine(CloudSim.clock() + ": " + getName() 
				+ ": VSwitch Created: " +  vswitch.getName() + " in " + pswitch.getName());

//		createNewFlows();
		
		createFlowsBetweenVms();
		
	}
	
	private void createFlowsBetweenVms() {
		for (Vm vm: vmList) {
			createFlowsForVm(vm, vm.getUserId());
		}
	}
	
	private void createFlowsForVm(Vm vm, int userId) {
		VirtualTopology virtualTopology = virtualTopologies.get(userId);
		List<List<Arc>> links = virtualTopology.getPathsFromVm(vm.getId());
		VdcEmbedding embedding = vdcEmbeddingMap.get(virtualTopology);
		Map<Arc, List<Link>> vlinkMap = embedding.getVLinkMap();
		System.out.println("embedding: " + embedding);
		System.out.println("vLinkMap: " + vlinkMap);
		int flowId;
		if (links == null) {
			return;
		}
		for (List<Arc> vlinksForOnePairOfVms: links) {
			Arc lastVLink = vlinksForOnePairOfVms.get(vlinksForOnePairOfVms.size()-1);
			int srcVmId = vm.getId();
			int destVmId = lastVLink.getDstId();
			int pathLength = 0;
			if (!virtualTopology.getVmsTable().containsKey(destVmId)) {
				destVmId = lastVLink.getSrcId();
			}
			Node srcNode = findSDNHost(vm.getId()), nextHop;
			if (checkFlowExists(srcVmId, destVmId)) {
				continue;
			}
			SDNHost srcHost = findSDNHost(srcVmId);
			SDNHost dstHost = findSDNHost(destVmId);
			if (srcHost == null || dstHost == null) {
				continue;
			}
			flowNumbers++;
			flowId = flowNumbers;
			for (Arc vlink: vlinksForOnePairOfVms) {
				System.out.println("vlink: " + vlink);
				List<Link> plinks = vlinkMap.get(vlink);
				for (Link link: plinks) { 
					System.out.println(srcNode);
					System.out.println("plink: " + link);
					nextHop = link.getOtherNode(srcNode);
					srcNode.addVMRoute(srcVmId, destVmId, flowId, nextHop);
					srcNode = nextHop;
					++pathLength;
				}
			}
			System.out.println("New flow with FlowId = " + flowId + " between " + srcVmId + " " + destVmId + " created");
			updateVSwitchesInFlow(srcHost, srcVmId, destVmId, flowId, pathLength);
			System.out.println("VSwitches updated for flow.");
			Arc arc = new Arc(Integer.toString(flowId), srcVmId, destVmId, flowId, 0, 0);
			newFlows.add(arc);
			this.flowIdArcTable.put(flowId, arc);
		}
	}
	
}
