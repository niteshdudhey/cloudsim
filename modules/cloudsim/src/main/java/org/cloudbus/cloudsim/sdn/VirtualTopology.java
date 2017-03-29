/**
 * 
 */
package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Vm;

import com.google.common.collect.Lists;

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
	
	// Adjacency List representation of the Virtual Topology Graph
	ArrayList<ArrayList<Integer>> adjList;
	
	// Node id in graph -> GraphNode object
	Map<Integer, GraphNode> graphNodes;
	
	// Assuming no two Arcs are between the same pair of nodes
	// GraphNode id 1, GraphNode id 2 -> Arc
	Map<Pair<Integer, Integer>, Arc> graphLinksMap;
	
	public VirtualTopology(int datacenterId, String datacenter) {
		this.datacenterId = datacenterId;
		this.datacenter = datacenter;
		vmsTable = new HashMap<Integer, Vm>();
		vswitchesTable = new HashMap<Integer, VSwitch>();
		vlinksTable = new HashMap<Integer, Arc>();
		adjList = new ArrayList<ArrayList<Integer>>();
		graphNodes = new HashMap<Integer, GraphNode>();
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
	
	private void createAdjList() {
		int idx = 0;
		adjList.clear();
		for (Map.Entry<Integer, Vm> entry: vmsTable.entrySet()) {
			if (((TimedVm)entry.getValue()).isActive()) {
				graphNodes.put(idx, new GraphNode(idx, entry.getKey()));
				++idx;
			}
		}
		for (Map.Entry<Integer, VSwitch> entry: vswitchesTable.entrySet()) {
			if (entry.getValue().isActive()) {
				graphNodes.put(idx, new GraphNode(idx, entry.getKey()));
				++idx;
			}
		}
		for (Map.Entry<Integer, Arc> entry: vlinksTable.entrySet()) {
			Arc vlink = entry.getValue();
			GraphNode src = graphNodes.get(vlink.getSrcId());
			GraphNode dst = graphNodes.get(vlink.getDstId());
			if (src == null || dst == null) {
				continue;
			}
			int srcId = src.getGraphNodeId();
			int dstId = dst.getGraphNodeId();
			adjList.get(srcId).add(dstId);
//			adjList.get(dstId).add(srcId);
			graphLinksMap.put(new Pair<Integer, Integer>(srcId, dstId), vlink);
//			graphLinksMap.put(new Pair<Integer, Integer>(dstId, srcId), vlink);
		}
	}
	
//	private ArrayList<ArrayList<Integer>> getAdjacencyList() {
//		return adjList;
//	}
	
	public List<List<Arc>> getPathsFromVm(int vmId) {
		int i, startId = graphNodes.get(vmId).getGraphNodeId();
		List<List<Arc>> allPaths = new ArrayList<List<Arc>>();
		createAdjList();
		dfs(startId);
		for (i = 0; i < adjList.size(); ++i) {
			if (graphNodes.get(i).getVisited() && i != startId && checkIdOfVm(i)) {
				allPaths.add(makePath(startId, i));
			}
		}
		return null;
	}
	
	private boolean checkIdOfVm(int graphNodeId) {
		if (graphNodes.get(graphNodeId) == null) {
			return false;
		}
		return vmsTable.containsKey(graphNodes.get(graphNodeId).getVNodeId());
	}
	
	private List<Arc> makePath(int srcId, int dstId) {
		List<Arc> path = new ArrayList<Arc>();
		int nodeId = dstId, parentId;
		while (nodeId != srcId) {
			parentId = graphNodes.get(nodeId).getParent().getGraphNodeId();
			path.add(graphNodes.get(nodeId).getVLinkFromParent());
			nodeId = parentId;
		}
		// Note that the VLinks are added in reverse order above.
		return Lists.reverse(path);
	}
	
	private void dfs(int start) {
		int i, j;
		GraphNode parent = graphNodes.get(start);
		parent.setVisited(true);
		for (i = 0; i < adjList.get(start).size(); ++i) {
			j = adjList.get(start).get(i);
			GraphNode child = graphNodes.get(j);
			if (!child.getVisited()) {
				child.setParentNode(parent);
				child.setVLinkFromParent(graphLinksMap.get(new Pair<Integer, Integer>(start, j)));
				dfs(j);
			}
		}
	}
	
	private class GraphNode {
		int graphNodeId;
		int vNodeId;
		boolean visited;
		GraphNode parent;
		Arc vlinkFromParent;
		public GraphNode(int graphNodeId, int vNodeId) {
			this.graphNodeId = graphNodeId;
			this.vNodeId = vNodeId;
			visited = false;
			parent = null;
		}
		public int getGraphNodeId() {
			return graphNodeId;
		}
		public int getVNodeId() {
			return vNodeId;
		}
		public boolean getVisited() {
			return visited;
		}
		public GraphNode getParent() {
			return parent;
		}
		public Arc getVLinkFromParent() {
			return vlinkFromParent;
		}
		public void setVisited(boolean visited) {
			this.visited = visited;
		}
		public void setParentNode(GraphNode parent) {
			this.parent = parent;
		}
		public void setVLinkFromParent(Arc arc) {
			this.vlinkFromParent = arc;
		}
	}
	
}
