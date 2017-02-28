/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.EventSummary;
import org.cloudbus.cloudsim.FullHostStateHistoryEntry;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;


/**
 * Extended class of Host to support SDN.
 * Added function includes data transmission after completion of Cloudlet compute processing.
 * 
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */
public class SDNHost extends SimEntity implements Node {
	private static final double PROCESSING_DELAY= 0.1;
		
	Host host;
	
	EdgeSwitch sw;
	//Hashtable<Integer,Vm> vms;
	
	Hashtable<Integer, Middlebox> middleboxes;
	
	Hashtable<Cloudlet, Request> requestsTable;
	
	ForwardingRule forwardingTable;
	
	RoutingTable routingTable;
	
	int rank = -1;
	
	NetworkOperatingSystem nos;

	private List<FullHostStateHistoryEntry> fullStateHistory;

	SDNHost(String name, Host host, NetworkOperatingSystem nos){
		//super("Host" + host.getId());
		super(name);
		
		this.host = host;
		this.nos = nos;
			
		//this.vms = new Hashtable<Integer,Vm>();
		this.middleboxes = new Hashtable<Integer, Middlebox>();
		this.requestsTable = new Hashtable<Cloudlet, Request>();
		this.forwardingTable = new ForwardingRule();
		this.routingTable = new RoutingTable();
		this.fullStateHistory = new LinkedList<FullHostStateHistoryEntry>();
	}
	
	public Host getHost(){
		return host;
	}
	
	public void setEdgeSwitch(EdgeSwitch sw){
		this.sw = sw;
	}
	
	/*public void addVm(Vm vm){
		vms.put(vm.getId(), vm);
		host.vmCreate(vm);
	}*/
	
	public void addMiddlebox(Middlebox m){
		middleboxes.put(m.getId(), m);
		host.vmCreate(m.getVm());
	}

	@Override
	public void startEntity(){}
	
	@Override
	public void shutdownEntity(){}

	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();
		EventSummary.storePresentState(CloudSim.clock());
		switch(tag){
			case Constants.SDN_PACKAGE:
				processPackage((Package) ev.getData()); 
				break;
			case CloudSimTags.CLOUDLET_RETURN: 
				processCloudletReturn((Cloudlet) ev.getData()); 
				break;
			default: 
				System.out.println("Unknown event received by " + super.getName() + ". Tag:" + ev.getTag());
		}
		EventSummary.storePresentState(CloudSim.clock());
	}
	
	private Vm findVm(int vmId) {
		List<Vm> vms = host.getVmList();
		
		for(Vm vm : vms) {
			if(vm.getId() == vmId) {
				return vm;
			}
		}
		return null;
	}
	
	private void processPackage(Package data) {
		int vmId = data.getDestination();
		Vm dstVm = findVm(vmId);
		
		if (dstVm != null) {
			// Try to deliver package to a hosted VM.
			
			//Log.printLine(CloudSim.clock() + ": " + getName() + ".processPackage(): Deliver the request to dest VM: "+ dstVm);
			data.setFinishTime(CloudSim.clock());
			
			Request req = data.getPayload();
			Activity ac = req.removeNextActivity();
			processActivity(ac, req, vmId);
		} 
		else if (middleboxes.containsKey(vmId)) {
			// Try to deliver package to a hosted middlebox.
			Request req = data.getPayload();
			Middlebox m = middleboxes.get(vmId);
			m.submitRequest(req);
		} 
		else {
			// Something wrong - package doesn't come from/goes to a VM from this Host.
			System.out.println("Warning package sent to wrong host. Host ID=" + host.getId() + " DST VM ID=" + vmId + ", SRC VM ID=" + data.getDestination());
		}
	}
	
	private void processCloudletReturn(Cloudlet data) {
		Request req = requestsTable.remove(data);
		if (req.isFinished()){
			//return to user
			send(req.getUserId(), PROCESSING_DELAY, Constants.REQUEST_COMPLETED, req);
		} 
		else {
			//consume next activity from request. It should be a transmission.
			Activity ac = req.removeNextActivity();
			processActivity(ac, req, data.getVmId());
		}
	}
	
	private void processActivity(Activity ac, Request req, int vmId) {
		if(ac instanceof Transmission) {
			Transmission tr = (Transmission)ac;

			Package pkg = tr.getPackage();
			//send package to router via channel (NOS)
			nos.addPackageToChannel(this, pkg);
			
			pkg.setStartTime(CloudSim.clock());
		}
		else if(ac instanceof Processing) {
				Cloudlet cl = ((Processing) ac).getCloudlet();
				cl.setVmId(vmId);
				
				requestsTable.put(cl, req);
				//sendNow(host.getDatacenter().getId(), CloudSimTags.CLOUDLET_SUBMIT, cl);
				sendNow(nos.getDatacenterIdFromBrokerId(cl.getUserId()), CloudSimTags.CLOUDLET_SUBMIT, cl);
		} 
		else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Activity is unknown..");
		}
	}
	
	@Override
	public void storeCurrentState(double time) {
		
		FullHostStateHistoryEntry stateHistory = new FullHostStateHistoryEntry(time);
		
		/*
		 * Ram related attributes
		 */
		stateHistory.setRam(host.getRam());
		stateHistory.setAvailableRam(host.getRamProvisioner().getAvailableRam());
		int totalRequestedRam = 0;
		for (Vm vm: host.getVmList()) {
			totalRequestedRam += vm.getRam();
		}
		stateHistory.setRequestedRam(totalRequestedRam);
		
		/*
		 * Mips related attributes
		 */
		stateHistory.setMips(host.getTotalMips());
		List<Double> availableMipsList = new ArrayList<Double>();
		double availableMips = 0.0;
		double totalRequestedMips = 0.0;
		for (Pe pe: host.getPeList()) {
			double mips = pe.getPeProvisioner().getAvailableMips();
			availableMips += mips;
			availableMipsList.add(mips);
		}
		for (Vm vm: host.getVmList()) {
			totalRequestedMips += host.getTotalAllocatedMipsForVm(vm);
		}
		stateHistory.setAvailableMipsList(availableMipsList);
		stateHistory.setAvailableMips(availableMips);
		stateHistory.setRequestedMips(totalRequestedMips);
		
		/*
		 * Bw related attributes
		 * Not network bw related
//		 */
		stateHistory.setBw(host.getBw());
		stateHistory.setAvailableBw(host.getBwProvisioner().getAvailableBw());
		long totalRequestedBw = 0;
		for (Vm vm: host.getVmList()) {
			totalRequestedBw += vm.getBw();
		}
		stateHistory.setRequestedBw(totalRequestedBw);
				
		/*
		 * Utilization attributes
		 * TODO: Need complete revamp
		 */
		double totalCpuUtil = 0.0;
		for (Vm vm: host.getVmList()) {
			totalCpuUtil += vm.getTotalUtilizationOfCpu(time);
		}
		stateHistory.setCpuUtil(totalCpuUtil);
		
		double totalRamUtil = 0.0;
		for (Vm vm: host.getVmList()) {
			totalRamUtil += 
					(vm.getCloudletScheduler().getCurrentRequestedUtilizationOfRam())
					*(vm.getRam());
		}
		totalRamUtil /= host.getRam();
		stateHistory.setRamUtil(totalRamUtil);
		
		// Commented code doesn't calculate network bandwidth utilization
//		double totalBwUtil = 0.0;
//		for (Vm vm: host.getVmList()) {
//			totalBwUtil += 
//					(vm.getCloudletScheduler().getCurrentRequestedUtilizationOfBw())
//					*(vm.getBw());
//		}
//		totalBwUtil /= host.getBw();
//		stateHistory.setBwUtil(totalBwUtil);
		
		Collection<Link> allLinks = nos.getPhysicalTopology().getAllLinks();
		Link reqdLink1 = null;
		Link reqdLink2 = null;
		for (Link link: allLinks) {
			if (link.getLowOrder() == this) {
				reqdLink1 = link;
				break;
			} else if (link.getHighOrder() == this) {
				reqdLink2 = link;
			}
		}
		
//		System.out.println("Free Bw in link " + reqdLink.getName() + "= " + reqdLink.getFreeBandwidth(this));
		
		double availableBw1 = getBandwidth();
		double availableBw2 = getBandwidth();
//		if (sw != null) {
//			List<Link> linkToSwitch = sw.getRoute(this);
//			System.out.println("All relevant links b/w switch and host: ");
//			for (Link link1: linkToSwitch) {
//				System.out.println(link1.getName());
//				System.out.println(link1.getHighOrder());
//				System.out.println(link1.getLowOrder());
//			}
//			availableBw = linkToSwitch.get(0).getFreeBandwidth(this);
//		}
		if (reqdLink1 != null) {
			availableBw1 = reqdLink1.getFreeBandwidth(this);
		}
		if (reqdLink2 != null) {
			availableBw2 = reqdLink2.getFreeBandwidth(this);
		}
		
//		stateHistory.setBwUtil(1.0-(availableBw1+availableBw2)/2*getBandwidth());
		
		stateHistory.setUpBwUtil(1.0-availableBw1/getBandwidth());
		stateHistory.setDownBwUtil(1.0-availableBw2/getBandwidth());
		
		List<Integer> vmIdsList = new ArrayList<Integer>();
		for (Vm vm: host.getVmList()) {
			vmIdsList.add(vm.getId());
		}
		stateHistory.setVmIdsList(vmIdsList);
		
		/*
		 * State History stored for the given time instant
		 */
		fullStateHistory.add(stateHistory);
//		System.out.println("Host " + getId() + " state stored at time " + time);
		
	}
	
	public List<FullHostStateHistoryEntry> getFullHostStateHistory() {
		return fullStateHistory;
	}

	/******* Routeable interface implementation methods ******/

	@Override
	public int getAddress() {
		return super.getId();
	}
	
	@Override
	public long getBandwidth() {
		return host.getBw();
	}

	@Override
	public void clearVMRoutingTable() {
		this.forwardingTable.clear();
	}

	@Override
	public void addVMRoute(int src, int dest, int flowId, Node to){
		forwardingTable.addRule(src, dest, flowId, to);
	}
	
	@Override
	public Node getVMRoute(int src, int dest, int flowId){
		Node route= this.forwardingTable.getRoute(src, dest, flowId);
		
		if(route == null) {
			this.printVMRoute();
			System.err.println("SDNHost: ERROR: Cannot find route:" + src + "->" + dest + ", flow =" + flowId);
		}
			
		return route;
	}
	
	@Override
	public void removeVMRoute(int src, int dest, int flowId){
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
		return "SDNHost:" + this.getName();
	}

	@Override
	public void addLink(Link l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateNetworkUtilization() {
		// TODO Auto-generated method stub
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
}
