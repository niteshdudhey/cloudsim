/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.EventSummary;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.HostSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.LinkSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.PdcSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.SwitchSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VLinkSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VSwitchSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VdcSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VmSpec;
import org.cloudbus.cloudsim.sdn.example.policies.VmSchedulerTimeSharedEnergy;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * NOS calculates and estimates network behaviour. It also mimics SDN Controller functions.  
 * It manages channels between switches, and assigns packages to channels and control their completion
 * Once the transmission is completed, forward the packet to the destination.
 *  
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */

/**
 * Modified to handle multiple Datacenters simultaneously.
 * 
 * @author Nitesh Dudhey
 *
 */
public abstract class NetworkOperatingSystem extends SimEntity {

	static final int MAX_PORTS = 256;
	
	// Used to assign flow Ids to Arcs.
	// TOCHECK: whether it should necessarily be static.
	protected static int flowNumbers = 0;
		
	String physicalTopologyFileName; 
	
	protected PhysicalTopology topology;
	
	// UserId -> VirtualTopology.
	protected Map<Integer, VirtualTopology> virtualTopologies;
	
	protected VdcEmbedder embedder;
	
	protected Map<VirtualTopology, VdcEmbedding> vdcEmbeddingMap;
	
	// Each broker/user is associated with one Datacenter.
	protected Map<Integer, Integer> brokerIdToDatacenterIdMap;
	
	// Multiple Datacenters on one NOS.
	protected Map<Integer, SDNDatacenter> datacenterIdToDatacenterMap;
	
	Hashtable<Package, Node> pkgTable;
	
	Hashtable<String, Channel> channelTable;

	List<Host> hosts;
	
	protected List<SDNHost> sdnhosts;
	
	protected List<Switch> switches = new ArrayList<Switch>();
	
	int virtualNodeId = 0;

	// Some of these members could be redundant due to the introduction of VirtualTopologies.
	// Could be removed later.
	
	int vmId = 0;

	protected LinkedList<Vm> vmList;
	
	protected LinkedList<Arc> arcList;
	
	protected LinkedList<VSwitch> vswitchList;
	
	protected Map<Integer, Arc> flowIdArcTable;
	
	Map<String, Integer> hostNameIdTable;
	
	Map<String, Integer> vmNameIdTable;
	
	Map<Integer, String> vswitchIdNameTable;
	
	Map<String, Integer> vswitchNameIdTable;
	
	Map<String, Integer> flowNameIdTable;
	
	Map<Integer, String> vmIdRequestedHostTable;
	
	protected Map<Integer, List<VSwitch> > flowIdVSwitchListTable;
	
	protected List<Arc> newFlows;
	
	public static Map<Integer, String> debugVmIdName = new HashMap<Integer, String>();
	
	public static Map<Integer, String> debugFlowIdName = new HashMap<Integer, String>();
	
	boolean isApplicationDeployed = false;
	
	// Resolution of the result.
	public static double minTimeBetweenEvents = 0.001;		// in sec
	public static int resolutionPlaces = 5;
	public static int timeUnit = 1;							// 1: sec, 1000: msec
	
	public NetworkOperatingSystem(String physicalTopologyFileName, VdcEmbedder embedder) {
		super("NOS");
		
		this.physicalTopologyFileName = physicalTopologyFileName;
		this.embedder = embedder;
		
		this.pkgTable = new Hashtable<Package, Node>();
		this.channelTable = new Hashtable<String, Channel>();
		
		this.brokerIdToDatacenterIdMap = new HashMap<Integer, Integer>();
		this.datacenterIdToDatacenterMap = new HashMap<Integer, SDNDatacenter>();
		
		initPhysicalTopology();
		
		virtualTopologies = new HashMap<Integer, VirtualTopology>();
		
		vmNameIdTable = new HashMap<String, Integer>();
		vmList = new LinkedList<Vm>();
		
		arcList = new LinkedList<Arc>();
		flowIdArcTable = new HashMap<Integer, Arc>();
		
		vswitchIdNameTable = new HashMap<Integer, String>();
		vswitchNameIdTable = new HashMap<String, Integer>();
		vswitchList = new LinkedList<VSwitch>();
		
		newFlows = new LinkedList<Arc>();
		
		flowNameIdTable = new HashMap<String, Integer>();
		flowNameIdTable.put("default", -1);
		
		flowIdVSwitchListTable = new HashMap<Integer, List<VSwitch> >();
		
		vdcEmbeddingMap = new HashMap<VirtualTopology, VdcEmbedding>();
		
		EventSummary.setSDNHostList(sdnhosts);
		EventSummary.setLinks(getPhysicalTopology().getAllLinks());
		EventSummary.setSwitchList(switches);
	}

	/**
	 * 1. map VMs and middleboxes to hosts, add the new vm/mb to the vmHostTable, advise host, advise dc
	 * 2. set channels and bws
	 * 3. set routing tables to restrict hops to meet latency
	 */
	// TODO: Need to remove the arguments that are redundant.
	protected abstract boolean deployApplication(VirtualTopology virtualTopology);
	
	protected abstract boolean deployApplication(List<Vm> vms, List<Middlebox> middleboxes, List<Arc> links, List<VSwitch> vswitchList);
	
	// Depricated.
	protected abstract boolean deployApplication(List<Vm> vms, List<Middlebox> middleboxes, List<Arc> links, VirtualTopology virtualTopology);
	
	protected abstract Middlebox deployMiddlebox(String type, Vm vm);

	public PhysicalTopology getPhysicalTopology(){
		return topology;
	}

	public static double getMinTimeBetweenNetworkEvents() {
	    return minTimeBetweenEvents* timeUnit;
	}
	
	public static double round(double value) {
		int places = resolutionPlaces;
	    
		if (places < 0) {
			throw new IllegalArgumentException();
		}

		if(timeUnit >= 1000) {
			value = Math.floor(value*timeUnit);
		}
		
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.CEILING);
	    
	    return bd.doubleValue();
	}
		
	@Override
	public void startEntity() {}

	@Override
	public void shutdownEntity() {}
	
	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();
		
		EventSummary.storePresentState(CloudSim.clock());
		System.out.println("handling event in NOS. Event Tag " + tag);
		
		switch(tag){
			case Constants.SDN_INTERNAL_PACKAGE_PROCESS: 
				internalPackageProcess(); 
				break;
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreateAck(ev);
				break;
			case CloudSimTags.VM_DESTROY:
				processVmDestroyAck(ev);
				embedder.deallocateVm(topology, (TimedVm) ev.getData());
				break;
			case CloudSimTags.VSWITCH_CREATE_ACK:
				processVSwitchCreateAck(ev);
				break;
			case CloudSimTags.VSWITCH_DESTROY:
				processVSwitchDestroyAck(ev);
				break;
			default: 
				System.out.println("Unknown event received by " + super.getName() + ". Tag:" + ev.getTag());
		}
		
		EventSummary.storePresentState(CloudSim.clock());
	}

	public void processVmCreateAck(SimEvent ev) {
	}
	
	protected void processVmDestroyAck(SimEvent ev) {
		Vm destroyedVm = (Vm) ev.getData();
		
		// Remove all channels transferring data from or to this VM.
		for(Vm vm : this.vmList) {
			Channel ch = this.findChannel(vm.getId(), destroyedVm.getId(), -1);
			
			if(ch != null) {
				this.removeChannel(getKey(vm.getId(), destroyedVm.getId(), -1));
			}

			ch = this.findChannel(destroyedVm.getId(), vm.getId(), -1);
			if(ch != null) {
				this.removeChannel(getKey(destroyedVm.getId(), vm.getId(), -1));
			}
		}
		
		sendInternalEvent();
	}
	
	// TODO: Need to handle data transfer b/w VSwitches and VMs in NOS (and/or SNOS)
	public void processVSwitchCreateAck(SimEvent ev) {
		
	}
	
	// Important TODO: Need to remove all channels that are transmitting 
	// through the given virtual switch
	public void processVSwitchDestroyAck(SimEvent ev) {
		VSwitch destroyedVSwitch = (VSwitch) ev.getData();
		
		// Remove all channels transferring data from or to this VSwitch.
		for(VSwitch vswitch : this.vswitchList) {
//			Channel ch = this.findChannel(vm.getId(), destroyedVm.getId(), -1);
//			
//			if(ch != null) {
//				this.removeChannel(getKey(vm.getId(), destroyedVm.getId(), -1));
//			}
//
//			ch = this.findChannel(destroyedVm.getId(), vm.getId(), -1);
//			if(ch != null) {
//				this.removeChannel(getKey(destroyedVm.getId(), vm.getId(), -1));
//			}
		}
		
//		sendInternalEvent();
	}

	public void addPackageToChannel(Node sender, Package pkg) {
		int src = pkg.getOrigin();
		int dst = pkg.getDestination();
		int flowId = getFlowIdForVms(src, dst);
		pkg.setFlowId(flowId);
					
		if(sender.equals(sender.getVMRoute(src, dst, flowId))) {
			// For loopback packet (when src and dst is on the same host).
			sendNow(sender.getAddress(), Constants.SDN_PACKAGE, pkg);
			
			return;
		}
		
		updatePackageProcessing();
		
		Channel channel = findChannel(src, dst, flowId);
		
		if(channel == null) {
			// No channel establisihed. Add a channel.
			channel = createChannel(src, dst, flowId, sender);
			
			if(channel == null) {
				// Failed to create channel.
				return;
			}
			
			addChannel(src, dst, flowId, channel);
		}
		
		if (!pkgTable.contains(pkg)) {
			pkgTable.put(pkg, sender);
			send(sender.getAddress(), channel.getDelay(), Constants.PACKET_DELAY, pkg);
			return;
		}
		
		double eft = channel.addTransmission(new Transmission(pkg));
		
		double diff = channel.getAllocatedBandwidthPerTransmission();
		TimedVm upvm = (TimedVm) findVm(pkg.getOrigin());
		TimedVm downvm = (TimedVm) findVm(pkg.getDestination());
		upvm.incrementCurrentUpBW(diff);
		downvm.incrementCurrentDownBW(diff);
		
		Log.printLine(CloudSim.clock() + ": " + getName() + ".addPackageToChannel (" + channel + "): Transmission added:" 
					+ NetworkOperatingSystem.debugVmIdName.get(src) + "->" 
					+ NetworkOperatingSystem.debugVmIdName.get(dst) + ", flow=" + flowId + " / eft=" + eft);

		sendInternalEvent();
	}
	
	private void internalPackageProcess() {
		if(updatePackageProcessing()) {
			sendInternalEvent();
		}
	}
	
	private void sendInternalEvent() {
		CloudSim.cancelAll(getId(), new PredicateType(Constants.SDN_INTERNAL_PACKAGE_PROCESS));
		
		if(channelTable.size() != 0) {
			// More to process. Send event again.
			double delay = this.nextFinishTime();
			Log.printLine(CloudSim.clock() + ": " + getName() + ".sendInternalEvent(): next finish time: " + delay);
			
			send(this.getId(), delay, Constants.SDN_INTERNAL_PACKAGE_PROCESS);
		}
	}
	
	private double nextFinishTime() {
		double earliestEft = Double.POSITIVE_INFINITY;
		
		for(Channel ch : channelTable.values()){
			
			double eft = ch.nextFinishTime();
			if (eft < earliestEft){
				earliestEft = eft;
			}
		}
		
		if(earliestEft == Double.POSITIVE_INFINITY) {
			throw new IllegalArgumentException("NOS.nextFinishTime(): next finish time is infinite!");
		}
		
		return earliestEft;
	}
	
	private boolean updatePackageProcessing() {
		boolean needSendEvent = false;
		
		LinkedList<Channel> completeChannels = new LinkedList<Channel>();
		
		for(Channel ch : channelTable.values()){
			boolean isCompleted = ch.updatePackageProcessing();
			needSendEvent = needSendEvent || isCompleted;
			
			completeChannels.add(ch);
		}
		
		if(completeChannels.size() != 0) {
			processCompletePackages(completeChannels);
			updateChannel();
		}

		return needSendEvent;
	}
	
	private void processCompletePackages(List<Channel> channels){
		for(Channel ch : channels) {
			
			Node dest = ch.getLastNode();
			
			for (Transmission tr : ch.getArrivedPackages()){
				Package pkg = tr.getPackage();
				
				double diff = -1 * tr.getFinishTimeBW();
				TimedVm upvm = (TimedVm) findVm(pkg.getOrigin());
				TimedVm downvm = (TimedVm) findVm(pkg.getDestination());
				upvm.incrementCurrentUpBW(diff);
				downvm.incrementCurrentDownBW(diff);
				
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Package completed: " + pkg + ". Send to destination:" + dest);
				sendNow(dest.getAddress(), Constants.SDN_PACKAGE, pkg);
			}
		}
	}
	
	public Map<Integer, String> getRequestedHostTable() {
        return this.vmIdRequestedHostTable;
    }
    
	public Map<String, Integer> getHostNameIdTable() {
    	return this.hostNameIdTable;
    }
	
	public Map<String, Integer> getVmNameIdTable() {
		return this.vmNameIdTable;
	}
	
	public Map<String, Integer> getFlowNameIdTable() {
		return this.flowNameIdTable;
	}
	
	public Map<Integer, String> getvswitchIdNameTable() {
		return this.vswitchIdNameTable;
	}
	
	public Map<String, Integer> getvswitchNameIdTable() {
		return this.vswitchNameIdTable;
	}
	
	public Map<Integer, List<VSwitch> > getFlowIdVSwitchListTable() {
		return this.flowIdVSwitchListTable;
	}
	
	private Channel findChannel(int from, int to, int channelId) {
		// Check if there is a pre-configured channel for this application.
		Channel channel = channelTable.get(getKey(from, to, channelId));

		if (channel == null) {
			// There is no channel for specific flow, find the default channel for this link.
			channel = channelTable.get(getKey(from, to));
		}
		
		return channel;
	}
	
	private void addChannel(int src, int dst, int chId, Channel ch) {
		this.channelTable.put(getKey(src, dst, chId), ch);
		ch.initialize();
		adjustAllChannels();
	}
	
	private Channel removeChannel(String key) {
		Channel ch = this.channelTable.remove(key);
		ch.terminate();
		adjustAllChannels();
		return ch;
	}
	
	private void adjustAllChannels() {
		for(Channel ch : this.channelTable.values()) {
			double oldTransmissionBandwidth = ch.getAllocatedBandwidthPerTransmission();
			
			if(ch.adjustDedicatedBandwidthAlongLink()) {
				// Channel BW is changed. send event.
				double newTransmissionBandwidth = ch.getAllocatedBandwidthPerTransmission();
				double diff = newTransmissionBandwidth - oldTransmissionBandwidth;
				
				for(Transmission transmission : ch.getActiveTransmissions()){
					TimedVm upvm = (TimedVm) findVm(transmission.getPackage().getOrigin());
					TimedVm downvm = (TimedVm) findVm(transmission.getPackage().getDestination());
					
					upvm.incrementCurrentUpBW(diff);
					downvm.incrementCurrentDownBW(diff);
				}
			}
		}
		
		for(Channel ch : this.channelTable.values()) {
			double oldTransmissionBandwidth = ch.getAllocatedBandwidthPerTransmission();
			
			if(ch.adjustSharedBandwidthAlongLink()) {
				// Channel BW is changed. send event.
				double newTransmissionBandwidth = ch.getAllocatedBandwidthPerTransmission();
				double diff = newTransmissionBandwidth - oldTransmissionBandwidth;
				
				for(Transmission transmission : ch.getActiveTransmissions()){
					TimedVm upvm = (TimedVm) findVm(transmission.getPackage().getOrigin());
					TimedVm downvm = (TimedVm) findVm(transmission.getPackage().getDestination());
					
					upvm.incrementCurrentUpBW(diff);
					downvm.incrementCurrentDownBW(diff);
				}
			}
		}
	}

	private Channel createChannel(int src, int dst, int flowId, Node srcNode) {
		List<Node> nodes = new ArrayList<Node>();
		List<Link> links = new ArrayList<Link>();
		
		Node origin = srcNode;
		Node dest = origin.getVMRoute(src, dst, flowId);
		
		if(dest == null) {
			return null;
		}
		
		Link link;
		double lowestBw = Double.POSITIVE_INFINITY;
		double reqBw = 0;
		String chName = "default";
		
		if(flowId != -1) {
			Arc flow = this.flowIdArcTable.get(flowId);
			reqBw = flow.getBw();
			chName = flow.getName();
		}
				
		nodes.add(origin);
		
		while(true) {
			link = this.topology.getLink(origin.getAddress(), dest.getAddress());
			links.add(link);
			nodes.add(dest);
			
			if(lowestBw > link.getFreeBandwidth(origin)) {
				lowestBw = link.getFreeBandwidth(origin);
			}
		
			if(dest instanceof SDNHost) {
				break;
			}
			
			origin = dest;
			dest = origin.getVMRoute(src, dst, flowId);
		} 
		
		if(flowId != -1 && lowestBw < reqBw) {
			// Free bandwidth is less than required one.
			// Cannot make channel.
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Free bandwidth is less than required.(" + getKey(src, dst, flowId) + "): ReqBW=" + reqBw + "/ Free=" + lowestBw);
		}
		
		List<VSwitch> vswitchList = getFlowIdVSwitchListTable().get(flowId);
		
		Channel channel = new Channel(chName, flowId, src, dst, nodes, links, vswitchList, reqBw, debugVmIdName.get(src), debugVmIdName.get(dst));

		return channel;
	}
	
	private void updateChannel() {
		List<String> removeCh = new ArrayList<String>();  
		
		for(String key : this.channelTable.keySet()) {
			Channel ch = this.channelTable.get(key);
			
			if(ch.getActiveTransmissionNum() == 0) {
				// No more job in channel. Delete.
				removeCh.add(key);
			}
		}
		
		for(String key : removeCh) {
			removeChannel(key);
		}
	}
	
	private String getKey(int origin, int destination) {
		return origin + "-" + destination;
	}
	
	private String getKey(int origin, int destination, int appId) {
		return getKey(origin, destination) + "-" + appId;
	}

	public void addDatacenter(SDNDatacenter dc, int userId) {
		brokerIdToDatacenterIdMap.put(userId, dc.getId());
		datacenterIdToDatacenterMap.put(dc.getId(), dc);
	}
	
	public SDNDatacenter getDatacenterById(int datacenterId) {
		return datacenterIdToDatacenterMap.get(datacenterId);
	}
	
	public int getDatacenterIdFromBrokerId(int brokerId){
		
		if (brokerIdToDatacenterIdMap.containsKey(brokerId)){
			return brokerIdToDatacenterIdMap.get(brokerId);
		}
		return -1;
	}

	public List<Host> getHostList() {
		return this.hosts;		
	}
	
	public List<Vm> getVmList() {
		return this.vmList;		
	}

	public List<Switch> getSwitchList() {
		return this.switches;
	}
	
	public List<VSwitch> getVSwitchList() {
		return this.vswitchList;
	}

	public boolean isApplicationDeployed() {
		return isApplicationDeployed;
	}

	protected Vm findVm(int vmId) {
		for(Vm vm : vmList) {
			if(vm.getId() == vmId) {
				return vm;
			}
		}
		
		return null;
	}
	
	protected VSwitch findVSwitch(int vswitchId) {
		String name = getvswitchIdNameTable().get(vswitchId);
		for(VSwitch vswitch : vswitchList) {
			if (vswitch.getName().equals(name)) {
				return vswitch;
			}
		}
		
		return null;
	}
	
	protected Switch findSwitch(int vswitchId) {
		VSwitch vswitch = findVSwitch(vswitchId);
		for (Switch pswitch: switches) {
			if (pswitch.getVSwitchList().contains(vswitch)) {
				return pswitch;
			}
		}
		return null;
	}
	
	protected SDNHost findSDNHost(Host host) {
		for(SDNHost sdnhost : sdnhosts) {
			if(sdnhost.getHost().equals(host)) {
				return sdnhost;
			}
		}
		
		return null;
	}
	
	protected SDNHost findSDNHost(int vmId) {
		
		Vm vm = findVm(vmId);
		
		if(vm == null) {
			return null;
		}
		
		for(SDNHost sdnhost : sdnhosts) {
			if(sdnhost.getHost().equals(vm.getHost())) {
				return sdnhost;
			}
		}
		
		return null;
	}
	
	public int getHostAddressByVmId(int vmId) {
		Vm vm = findVm(vmId);
		
		if(vm == null) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Cannot find VM with vmId=" + vmId);
			return -1;
		}
		
		Host host = vm.getHost();
		SDNHost sdnhost = findSDNHost(host);
		
		if(sdnhost == null) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Cannot find SDN Host with vmId=" + vmId);
			return -1;
		}
		
		return sdnhost.getAddress();
	}
	
	/**
	 * Creates a host by reading host specifications.
	 * 
	 * @param hostId
	 * @param hostSpec
	 * @return
	 */
	protected Host createHost(int hostId, HostSpec hostSpec) {
		LinkedList<Pe> peList = new LinkedList<Pe>();
		int peId = 0;
		
		for(int i = 0 ; i < hostSpec.getPes() ; i++) {
			peList.add(new Pe(peId++, new PeProvisionerSimple(hostSpec.getMips())));
		}
		
		RamProvisioner ramPro = new RamProvisionerSimple(hostSpec.getRam());
		BwProvisioner bwPro = new BwProvisionerSimple(hostSpec.getBw());
		VmScheduler vmScheduler = new VmSchedulerTimeSharedEnergy(peList);		
		
		Host newHost = new Host(hostId, ramPro, bwPro, hostSpec.getStorage(), peList, vmScheduler);
		
		return newHost;		
	}
	
	// Depricated.
	// Not removed because it is overloaded in OverbookingNetworkOperating System.
	protected Host createHost(int hostId, int ram, long bw, long storage, long pes, double mips) {
		LinkedList<Pe> peList = new LinkedList<Pe>();
		int peId = 0;
		
		for(int i = 0 ; i < pes ; i++) {
			peList.add(new Pe(peId++, new PeProvisionerSimple(mips)));
		}
		
		RamProvisioner ramPro = new RamProvisionerSimple(ram);
		BwProvisioner bwPro = new BwProvisionerSimple(bw);
		VmScheduler vmScheduler = new VmSchedulerTimeSharedEnergy(peList);		
		
		Host newHost = new Host(hostId, ramPro, bwPro, storage, peList, vmScheduler);
		
		return newHost;		
	}
	
	/**
	 * Creates a switch by reading switch specifications.
	 * 
	 * @param switchSpec
	 * @return
	 */
	protected Switch createSwitch(SwitchSpec switchSpec){
		Switch sw = null;
		
		switch(switchSpec.getType()){
		case "core":
			sw = new CoreSwitch(switchSpec.getName(), switchSpec.getBw(), switchSpec.getIops(), switchSpec.getUpports(), switchSpec.getDownports(), switchSpec.getSwitchingDelay(), this);
			break;
		case "aggregate":
			sw = new AggregationSwitch(switchSpec.getName(), switchSpec.getBw(), switchSpec.getIops(), switchSpec.getUpports(), switchSpec.getDownports(), switchSpec.getSwitchingDelay(), this);
			break;
		case "edge":
			sw = new EdgeSwitch(switchSpec.getName(), switchSpec.getBw(), switchSpec.getIops(), switchSpec.getUpports(), switchSpec.getDownports(), switchSpec.getSwitchingDelay(), this);
			break;
		default:
			System.err.println("No switch found!");
			//throw new IllegalArgumentException("No switch found!");
		}
		
		return sw;
	}
	
	public int getVSwitchRank(VSwitchSpec vswitchSpec) {
		int rank = -1;
		switch(vswitchSpec.getType()) {
		case "core":
			rank = 0;
			break;
		case "aggregate":
			rank = 1;
			break;
		case "edge":
			rank = 2;
			break;
		default:
			System.err.println("Invalid type for VSwitch");
		}
		return rank;
	}
	
	/**
	 * Initializes the physical topology by reading the physical topology file.
	 * 
	 */
	protected void initPhysicalTopology() {
		this.topology = new PhysicalTopology();
		this.hosts = new ArrayList<Host>();
		this.sdnhosts = new ArrayList<SDNHost>();
		
		int hostId = 0;
		Hashtable<String, Integer> nameIdTable = new Hashtable<String, Integer>();
		
		Gson gson = new Gson();
		PdcSpec pdc = null;
		
		try {
			pdc = gson.fromJson(new FileReader(this.physicalTopologyFileName), PdcSpec.class);
		} 
		catch (JsonSyntaxException e1) {
			e1.printStackTrace();
		} 
		catch (JsonIOException e1) {
			e1.printStackTrace();
		} 
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		for(HostSpec hostSpec : pdc.getHosts()) {
			
			if(hostSpec.getNums() == 0) {
				// nums not specified => one host.
				
				Host host = createHost(hostId, hostSpec);
				
				String nodeName2 = hostSpec.getName();
				
				SDNHost sdnHost = new SDNHost(nodeName2, host, this);
				nameIdTable.put(nodeName2, sdnHost.getAddress());
				
				hostId++;
				
				topology.addNode(sdnHost);
				this.hosts.add(host);
				this.sdnhosts.add(sdnHost);
				
			}
			else{
				String nodeName = hostSpec.getName();
				String nodeName2 = nodeName;
				
				for(int n = 0 ; n < hostSpec.getNums() ; n++) {
					nodeName2 = nodeName + n;
					
					Host host = createHost(hostId, hostSpec);					
					
					SDNHost sdnHost = new SDNHost(nodeName2, host, this);
					nameIdTable.put(nodeName2, sdnHost.getAddress());
					
					hostId++;
					
					topology.addNode(sdnHost);
					this.hosts.add(host);
					this.sdnhosts.add(sdnHost);
					
				}
			}
		}
		
		for(SwitchSpec switchSpec : pdc.getSwitches()) {
			if(switchSpec.getUpports() == 0) {
				switchSpec.setUpports(MAX_PORTS);
			}
			
			if(switchSpec.getDownports() == 0) {
				switchSpec.setDownports(MAX_PORTS);
			}
			
			Switch sw = createSwitch(switchSpec);
			
			if(sw != null) {
				nameIdTable.put(switchSpec.getName(), sw.getAddress());
				topology.addNode(sw);
				this.switches.add(sw);
			}
		}	
			
		for(LinkSpec linkSpec : pdc.getLinks()) {
			
			int srcAddress = nameIdTable.get(linkSpec.getSource());
			int dstAddress = nameIdTable.get(linkSpec.getDestination());
			
			topology.addLink(srcAddress, dstAddress, linkSpec.getLatency());
		}
				
		topology.buildDefaultRouting();
		
		// Initialize the Embedder to get ready to serve VDC requests.
		embedder.init(topology);
	}
	
	/**
	 * Reads the virtual network from the file and stores in the required data in their respective members.
	 *  
	 * @param userId
	 * @param virtualTopologyFileName
	 */
	public void readVirtualNetwork(int userId, String virtualTopologyFileName) {

		Gson gson = new Gson();
		VdcSpec vdc = null;
		
		try {
			vdc = gson.fromJson(new FileReader(virtualTopologyFileName), VdcSpec.class);
		} 
		catch (JsonSyntaxException e1) {
			e1.printStackTrace();
		} 
		catch (JsonIOException e1) {
			e1.printStackTrace();
		} 
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		int datacenterId = brokerIdToDatacenterIdMap.get(userId);
		String datacenterName = datacenterIdToDatacenterMap.get(datacenterId).getName();
		
		VirtualTopology virtualTopology = new VirtualTopology(datacenterId, datacenterName);
		
		datacenterIdToDatacenterMap.get(datacenterId).setStartTime(vdc.getStarttime());
		datacenterIdToDatacenterMap.get(datacenterId).setEndTime(vdc.getEndtime());
		
		for(VmSpec vmSpec : vdc.getVms()) {
			
			if(vmSpec.getBw() == 0) {
				vmSpec.setBw(1000);
			}
			
			if(vmSpec.getEndtime() == 0) {
				vmSpec.setEndtime(Double.POSITIVE_INFINITY);
			}
			
			if(vmSpec.getNums() == 0){
				vmSpec.setNums(1);
			}
			
			String nodeName = vmSpec.getName();
			String nodeName2 = nodeName;
			int nums = vmSpec.getNums();
			
			for(int n = 0 ; n < nums ; n++){
				nodeName2 = nodeName + n;
				
				Vm vm = new TimedVm(virtualNodeId, nodeName2, vmSpec, userId, datacenterId, "VMM", new CloudletSchedulerTimeShared());
				
				vmNameIdTable.put(nodeName2, virtualNodeId);
				NetworkOperatingSystem.debugVmIdName.put(virtualNodeId, nodeName2);
				vmList.add(vm);
				virtualTopology.addVm(vm);
				virtualNodeId++;
			}
		}		
		
		for(VSwitchSpec vSwitchSpec : vdc.getVSwitches()) {
			
			if(vSwitchSpec.getBw() == 0) {
				vSwitchSpec.setBw(1000);
			}
			
			if(vSwitchSpec.getEndtime() == 0) {
				vSwitchSpec.setEndtime(Double.POSITIVE_INFINITY);
			}
						
			if(vSwitchSpec.getNums() == 0) {
				vSwitchSpec.setNums(1);
			}
				
			String nodeName = vSwitchSpec.getName();
			String nodeName2 = nodeName;
			
			for(int n = 0 ; n < vSwitchSpec.getNums() ; n++){
				nodeName2 = nodeName + n;
				
				Switch pswitch = getSwitchByName(vSwitchSpec.getPSwitchName());
				
				int rank = getVSwitchRank(vSwitchSpec);
				
				VSwitch vswitch = new VSwitch(virtualNodeId, userId, rank, vSwitchSpec, datacenterId, pswitch);
						
				vswitchList.add(vswitch);
				// We use virtualNodeId to represent a virtual node Id. Since Arcs can 
				// contain both VMs and VSwitches, we need to use distinct Ids for 
				// VMs and VSwitches.
				vswitchIdNameTable.put(virtualNodeId, nodeName2);
				vswitchNameIdTable.put(nodeName2, virtualNodeId);
				NetworkOperatingSystem.debugVmIdName.put(virtualNodeId, nodeName2);
				++virtualNodeId;
			}
		}
		
		for(VLinkSpec vLinkSpec : vdc.getLinks()){
			
			String src = vLinkSpec.getSource();
			String dst = vLinkSpec.getDestination();
			
			// Next few lines assume that a VM and VSwitch cannot have the same name.
			// It is a reasonable assumption, but must be kept in mind while creating
			// VDCs.
			
			Integer srcId = vmNameIdTable.get(src);
			Integer dstId = vmNameIdTable.get(dst);
			
			if (srcId == null) {
				srcId = vswitchNameIdTable.get(src);
			}
			if (dstId == null) {
				dstId = vswitchNameIdTable.get(dst);
			}
			
			// Default flow.
			int flowId = -1;
			
			if(vLinkSpec.getName() != null && !"default".equalsIgnoreCase(vLinkSpec.getName())) {
				flowId = flowNumbers++;
				flowNameIdTable.put(vLinkSpec.getName(), flowId);
			}
			
			// For now we are assuming that Arc are uni-directional, hence the user
			// must give 2 arcs to represent a bi-directional VLink.
			
			Arc arc = new Arc(vLinkSpec, srcId, dstId, flowId);
			arcList.add(arc);
			
			if(flowId != -1) {
				flowIdArcTable.put(flowId, arc);
			}
		}
		
		EventSummary.setVmList(vmList);
		EventSummary.setVSwitchList(vswitchList);
		
		virtualTopologies.put(userId,  virtualTopology);
	}

	
	/**
	 * Deploys the VDC (if possible according to the Embedding Policy).
	 * 
	 * @param userId
	 * @return
	 */
	public boolean deployApplication(int userId) {
		boolean result = deployApplication(virtualTopologies.get(userId));
		
		if (result) {
			isApplicationDeployed = true;
			return true;
		}
		
		return false;
	}
	
	public List<SDNHost> getSDNHostList() {
		return sdnhosts;
	}
	
	public Switch getSwitchByName(String name) {
		Switch retSwitch = null;
		for (Switch pswitch: getSwitchList()) {
			if (pswitch.getName().equals(name)) {
				retSwitch = pswitch;
			}
		}
		return retSwitch;
	}
	
	public int getFlowIdForVms(int srcId, int dstId) {
		for (Arc arc: newFlows) {
			if (arc.getSrcId() == srcId && arc.getDstId() == dstId) {
				return arc.getFlowId();
			}
		}
		for (Arc arc: arcList) {
			if (arc.getSrcId() == srcId && arc.getDstId() == dstId) {
				return arc.getFlowId();
			}
		}
		return -2;
	}
	
}
