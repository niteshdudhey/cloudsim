/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.EventSummary;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;


/**
 * This represents switches that maintain routing information.
 * Note that all traffic estimation is calculated within NOS class, not in Switch class.
 * Energy consumption of Switch is calculated in this class by utilization history.
 * 
 * 
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */
public class Switch extends SimEntity implements Node {
	
	private static double POWER_CONSUMPTION_IDLE = 66.7;
	private static double POWER_CONSUMPTION_PER_ACTIVE_PORT = 1; 
	/* based on CARPO: Correlation-Aware Power Optimization in Data Center Networks by Xiaodong Wang et al. */
	
	int bw;
	
	long iops;
	
	int currentBw;
	
	long currentIops;
	
	double previousTime;
	
	long numPackets;
	
	double switchingDelay;
	
	int rank = -1;
	
	int currentupports = 0;
	
	int currentdownports = 0;
	
	NetworkOperatingSystem nos;
	
	Node[] upports;
	Node[] downports;
	
	ArrayList<Link> links = new ArrayList<Link>();

	ForwardingRule forwardingTable;
	
	ModifiedForwardingRule modifiedForwardingTable;
	
	RoutingTable routingTable;	
	
	Hashtable<Package,Long> processingTable;
	
	List<VSwitch> vswitchList;
	
	private List<SwitchStateHistoryEntry> stateHistory;
	
	private List<Node> lowerNodes;
	
	private List<Node> upperNodes;
	
	public Switch(String name, int bw, long iops, int upports, int downports, double switchingDelay, NetworkOperatingSystem nos) {
		super(name);
		this.bw = bw;
		this.currentBw = bw;
		this.iops = iops;
		this.numPackets = 0;
		this.currentIops = iops;
		this.previousTime = 0.0;
		this.nos = nos;
		
		if (upports > 0) {
			this.upports = new Node[upports];
		}
		
		this.downports = new Node[downports];
		
		this.switchingDelay = switchingDelay;
		
		this.forwardingTable = new ForwardingRule();
		this.modifiedForwardingTable = new ModifiedForwardingRule();
		this.processingTable = new Hashtable<Package,Long>();
		this.routingTable = new RoutingTable();
		
		this.currentdownports = downports;
		this.currentupports = upports;
		
		this.stateHistory = new LinkedList<SwitchStateHistoryEntry>();
		
		this.vswitchList = new LinkedList<VSwitch>();
		
		this.upperNodes = new ArrayList<Node>();
		this.lowerNodes = new ArrayList<Node>();
	}
	
	@Override
	public void startEntity() {}
	
	@Override
	public void shutdownEntity() {}

	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();
		EventSummary.storePresentState(CloudSim.clock());
		switch(tag){
			/*case Constants.SDN_INTERNAL_PACKAGE_PROCESS: 
				internalPackageProcessing(); 
				break;
			case Constants.SDN_PACKAGE: 
				sendToBuffer((Package) ev.getData()); 
				break;*/
			default: 
				System.out.println("Unknown event received by " + super.getName() + ". Tag:" + ev.getTag());
		}
		EventSummary.storePresentState(CloudSim.clock());
	}

	public void addLink(Link l){
		this.links.add(l);
	}
	
	public void incrementNumPacketsTransferred(long num) {
		this.numPackets += num;
	}
	
	public long getNumPacketsTransferred() {
		return numPackets;
	}
	
	public List<SwitchStateHistoryEntry> getSwitchStateHistory() {
		return stateHistory;
	}
	
	@Override
	public void storeCurrentState(double time) {
		SwitchStateHistoryEntry stateHistory = new SwitchStateHistoryEntry(time, getNumPacketsTransferred());
		getSwitchStateHistory().add(stateHistory);
	}
	
	public List<Node> getLowerNodes() {
		return lowerNodes;
	}
	
	public List<Node> getUpperNodes() {
		return upperNodes;
	}
	
	public void addLowerNode(Node lowerNode) {
		if (lowerNode == null) {
			System.err.println("Switch cannot have null lower node.");
		} else {
			this.lowerNodes.add(lowerNode);
		}
	}
	
	public void addUpperNode(Node upperNode) {
		if (upperNode == null) {
			System.err.println("Switch cannot have null upper node.");
		} else {
			this.upperNodes.add(upperNode);
		}
	}
	
	/************************************************
	 *  Calculate Utilization history
	 ************************************************/
	private List<HistoryEntry> utilizationHistories = null;
	private static double powerOffDuration = 0; // If switch was idle for 1 hours, it's turned off.

	public class HistoryEntry {
		public double startTime;
		public int numActivePorts;
		
		HistoryEntry(double t, int n) { 
			startTime = t; 
			numActivePorts = n;
		}
	}
	
	public List<HistoryEntry> getUtilizationHisotry() {
		return utilizationHistories;
	}
	
	public double getUtilizationEnergyConsumption() {
		
		double total = 0;
		double lastTime = 0;
		int lastPort = 0;
		
		if(this.utilizationHistories == null) {
			return 0;
		}
		
		for(HistoryEntry h : this.utilizationHistories) {
			double duration = h.startTime - lastTime;
			double power = calculatePower(lastPort);
			double energyConsumption = power * duration;
			
			// Assume that the host is turned off when duration is long enough.
			if(duration > powerOffDuration && lastPort == 0) {
				energyConsumption = 0;
			}
			
			total += energyConsumption;
			lastTime = h.startTime;
			lastPort = h.numActivePorts;
		}
		return total / 3600;	// transform to Whatt*hour from Whatt*seconds.
	}
	
	public void updateNetworkUtilization() {
		this.addUtilizationEntry();
		
		double time = CloudSim.clock();
		
		for(Link l : this.links) {
			l.updateUtilizationHistory(time);
		}
	}

	public void addUtilizationEntryTermination(double finishTime) {
		if(this.utilizationHistories != null) {
			this.utilizationHistories.add(new HistoryEntry(finishTime, 0));		
		}
	}

	private void addUtilizationEntry() {
		double time = CloudSim.clock();
		int totalActivePorts = getTotalActivePorts();
		
		if(utilizationHistories == null) {
			utilizationHistories = new ArrayList<HistoryEntry>();
		}
		else {
			HistoryEntry hist = this.utilizationHistories.get(this.utilizationHistories.size() - 1);
			
			if(hist.numActivePorts == totalActivePorts) {
				return;
			}
		}		
		
		this.utilizationHistories.add(new HistoryEntry(time, totalActivePorts));
	}
	
	private double calculatePower(int numActivePort) {
		double power = POWER_CONSUMPTION_IDLE + POWER_CONSUMPTION_PER_ACTIVE_PORT * numActivePort;
		return power;
	}
	
	private int getTotalActivePorts() {
		int num = 0;
		
		for(Link l : this.links) {
			if(l.isActive()) {
				num++;
			}
		}
		
		return num;
	}
	
	public double getSwitchingDelay() {
		return switchingDelay;
	}
	
	/* 
	 * TODO: Need to create a one-one mapping between Switch and 
	 * VSwitch ports. This will require a lot of changes in the Switch class,
	 * not limited to mapping the links (or nodes) to the up/down-ports.
	 */
	public boolean vswitchCreate(VSwitch vswitch) {
		boolean flag = true;
//		if (currentupports < vswitch.getUpports()) {
//			flag = false;
//		}
//		if (currentdownports < vswitch.getDownports()) {
//			flag = false;
//		}
		if (currentBw < vswitch.getBw()) {
			flag = false;
		}
		if (currentIops < vswitch.getIops()) {
			flag = false;
		}
		if (flag) {
//			currentupports -= vswitch.getUpports();
//			currentdownports -= vswitch.getDownports();
			currentBw -= vswitch.getBw();
			currentIops -= vswitch.getIops(); 
			getVSwitchList().add(vswitch);
		}
		return flag;
	}
	
	public boolean vswitchDestroy(VSwitch vswitch) {
		if (getVSwitchList().contains(vswitch)) {
//			currentupports += vswitch.getUpports();
//			currentdownports += vswitch.getDownports();
			currentBw += vswitch.getBw();
			currentIops += vswitch.getIops();
			getVSwitchList().remove(vswitch);
			return true;
		} else {
			return false;
		}
	}
	
	public List<VSwitch> getVSwitchList() {
		return vswitchList;
	}
	
	/*
	private void updateTime(double now) {
		this.previousTime = now;
	}
	private void internalPackageProcessing() {
		if(updatePackageProcessing()) {
			sendInternalEvent();
		}
		else {
			System.err.println(CloudSim.clock() + ": " + getName() +": Nothing changed! omg");
			sendInternalEvent();
		}
	}
	
	private void sendInternalEvent() {
		CloudSim.cancelAll(getId(), new PredicateType(Constants.SDN_INTERNAL_PACKAGE_PROCESS));		
		if(processingTable.size() != 0) {
			// More to process. Send event again
			double delay = this.nextFinishTime();
			Log.printLine(CloudSim.clock() + ": " + getName() + ".sendInternalEvent(): next finish time: "+ delay);
			send(this.getId(), delay, Constants.SDN_INTERNAL_PACKAGE_PROCESS);
		}
	}
	
	// Return if anything removed?
	private boolean updatePackageProcessing() {
		double currentTime = CloudSim.clock();
		double timeSpent = CloudSim.round(currentTime - this.previousTime);
		
		if(timeSpent <= 0 || processingTable.size() == 0)
			return false;	// Nothing changed
		
		//update the amount of iops processed this round
		long processedThisRound= Math.round(timeSpent * iops * MULTI / processingTable.size())+1;
		
		//update processing table; remove finished packs
		List<Package> toRemove = new ArrayList<Package>();
		for (Package key: processingTable.keySet()) {
			
			//DEBUG ONLY
			if(key.payload.requestId == 309) {
				System.out.println("ID:309 HERE:rem_len="+processingTable.get(key)+","+key.size);
			}
			
			long remainingLength = processingTable.get(key);
			remainingLength-=processedThisRound;
				
			if (remainingLength <= 0) {// finished: remove from the list
				toRemove.add(key);
			} else { //not finished:update table, check if it is the smaller to be processed
				processingTable.put(key, remainingLength);
			}
		}
	
		// Remove all packages that is done.
		for (Package pkg:toRemove){
			processingTable.remove(pkg);
			this.processPackageFinish(pkg);
		}
		updateTime(currentTime);

		
		System.err.println(CloudSim.clock()+": Switch.updatePackageProcessing("+getName()+ ") #("+this.processingTable.size()+"):Time spent:"+timeSpent+
				", Processed:"+processedThisRound);
		
		if(toRemove.isEmpty())
			return false;	// Nothing changed
		return true;
	}
	
	private void processPackageFinish(Package pkg) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": finished processing a package:" + pkg);
		nos.sendPackageToNextHop(this, pkg);
	}

	private void sendToBuffer(Package pkg) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Package received from Network:" + pkg);
		updatePackageProcessing();
		this.processingTable.put(pkg, pkg.getSize()*MULTI);
		sendInternalEvent();
	}
	
	private double nextFinishTime() {
		// Calculate the latest finish time among all jobs in the queue.
		long smallerPkg = Long.MAX_VALUE;
		
		for (Package key: processingTable.keySet()) {
			
			// DEBUG
			if(key.getPayload().requestId > 30 && key.getPayload().requestId < 35) {
				System.out.println("Here!");
			}
			
			if(key.getPayload().requestId > 530 && key.getPayload().requestId < 535) {
				System.out.println("Here!");
			}
			
			
			long remainingLength = processingTable.get(key);
			if (remainingLength<smallerPkg) smallerPkg=remainingLength;
		}
		
		double delay = (double)smallerPkg/ iops / MULTI * processingTable.size();//smallerPkg*processingTable.size()/(iops*MULTI);
		delay=CloudSim.round(delay);
		
		if (delay < CloudSim.getMinTimeBetweenEvents()) { 
			Log.printLine(CloudSim.clock() + ": " + getName() + ": delay is too short: "+ delay);
			delay = CloudSim.getMinTimeBetweenEvents();
		}
		
		return delay;
	}
	*/
	
	/******* Routeable interface implementation methods ******/
	
	@Override
	public int getAddress() {
		return super.getId();
	}
	
	@Override
	public long getBandwidth() {
		return bw;
	}
	
	public int getCurrentBandwidth() {
		return currentBw;
	}
	
	public long getCurrentIops() {
		return currentIops;
	}
	
	@Override
	public void clearVMRoutingTable(){
		this.forwardingTable.clear();
	}
	
	@Override
	public void addVMRoute(int src, int dest, int flowId, Node to){
		this.forwardingTable.addRule(src, dest, flowId, to);
	}
	
	@Override
	public Node getVMRoute(int src, int dest, int flowId){
		Node route= this.forwardingTable.getRoute(src, dest, flowId);
		
		if(route == null) {
			this.printVMRoute();
			System.err.println("SDNSwitch.getRoute() ERROR: Cannot find route:" + 
					NetworkOperatingSystem.debugVmIdName.get(src) + "->" +
					NetworkOperatingSystem.debugVmIdName.get(dest) + ", flow =" + flowId);
		}
			
		return route;
	}
	
	@Override
	public void removeVMRoute(int src, int dest, int flowId) {
		forwardingTable.removeRule(src, dest, flowId);
	}

	@Override
	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public int getRank() {
		return rank;
	}
	
	@Override
	public void printVMRoute() {
		forwardingTable.printForwardingTable(getName());
	}
	
	public String toString() {
		return "Switch: " + this.getName();
	}
	
	@Override
	public void addRoute(Node destHost, Link to) {
		this.routingTable.addRoute(destHost, to);
		
	}
	
	@Override
	public List<Link> getRoute(Node destHost) {
		return this.routingTable.getRoute(destHost);
	}
	
	@Override
	public RoutingTable getRoutingTable() {
		return this.routingTable;
	}
	
	@Override
	public void clearModifiedVMRoutingTable(){
	    this.modifiedForwardingTable.clear();
	}

	@Override
	public void addModifiedVMRoute(int flowId, Node from, Node to){
	    this.modifiedForwardingTable.addRule(flowId, from, to);
	}

	@Override
	public Node getModifiedVMRoute(int flowId, Node from){
	    Node route = this.modifiedForwardingTable.getRoute(flowId, from);
	    
	    if(route == null) {
	        this.printModifiedVMRoute();
	        System.err.println("SDNSwitch.getRoute() ERROR: Cannot find route for flow: " 
	        + flowId + " from src: " + NetworkOperatingSystem.debugVmIdName.get(from));
	    }
	        
	    return route;
	}

	@Override
	public void removeModifiedVMRoute(int flowId, Node from) {
	    modifiedForwardingTable.removeRule(flowId, from);
	}

	@Override
	public void printModifiedVMRoute() {
	    modifiedForwardingTable.printForwardingTable(getName());
	}
}
