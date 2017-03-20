/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
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
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VdcSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VmSpec;
import org.cloudbus.cloudsim.sdn.example.policies.VmSchedulerTimeSharedEnergy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
 * @author Nitesh Dudhey
 *
 */
public abstract class NetworkOperatingSystem extends SimEntity {

	static final int MAX_PORTS = 256;
	
	String physicalTopologyFileName; 
	
	protected PhysicalTopology topology;
	
	protected List<VirtualTopology> virtualTopologies;
	
	protected VdcEmbedder embedder;
	
	// Each broker/user is associated with one Datacenter.
	protected Map<Integer, Integer> brokerIdToDatacenterIdMap;
	
	// Multiple Datacenters on one NOS.
	protected Map<Integer, SDNDatacenter> datacenterIdToDatacenterMap;
	
	Hashtable<Package, Node> pkgTable;
	
	Hashtable<String, Channel> channelTable;

	List<Host> hosts;
	
	protected List<SDNHost> sdnhosts;
	
	protected List<Switch> switches = new ArrayList<Switch>();
	
	// Some of these members could be redundant due to the introduction of VirtualTopologies.
	// Could be removed later.
	
	int vmId = 0;
	
	protected LinkedList<Vm> vmList;
	
	protected LinkedList<Arc> arcList;
	
	Map<Integer, Arc> flowIdArcTable;
	
	Map<String, Integer> vmNameIdTable;
	
	Map<String, Integer> flowNameIdTable;
	
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
		
		virtualTopologies = new ArrayList<VirtualTopology>();
		
		vmNameIdTable = new HashMap<String, Integer>();
		vmList = new LinkedList<Vm>();
		
		arcList = new LinkedList<Arc>();
		flowIdArcTable = new HashMap<Integer, Arc>();
		
		flowNameIdTable = new HashMap<String, Integer>();
		flowNameIdTable.put("default", -1);
		
	}

	public PhysicalTopology getPhysicalTopology(){
		return topology;
	}

	/**
	 * 1. map VMs and middleboxes to hosts, add the new vm/mb to the vmHostTable, advise host, advise dc
	 * 2. set channels and bws
	 * 3. set routing tables to restrict hops to meet latency
	 */
	// TODO: Need to remove the arguments that are redundant.
	protected abstract boolean deployApplication(List<Vm> vms, List<Middlebox> middleboxes, List<Arc> links, VirtualTopology virtualTopology);
	protected abstract Middlebox deployMiddlebox(String type, Vm vm);

	
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
			default: 
				System.out.println("Unknown event received by " + super.getName() + ". Tag:" + ev.getTag());
		}
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

	public void addPackageToChannel(Node sender, Package pkg) {
		int src = pkg.getOrigin();
		int dst = pkg.getDestination();
		int flowId = pkg.getFlowId();
					
		if(sender.equals(sender.getVMRoute(src, dst, flowId))) {
			// For loopback packet (when src and dst is on the same host).
			sendNow(sender.getAddress(), Constants.SDN_PACKAGE, pkg);
			
			return;
		}
		
		updatePackageProcessing();
		
		pkgTable.put(pkg, sender);
		
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
		
		double eft = channel.addTransmission(new Transmission(pkg));
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
				
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Package completed: " + pkg + ". Send to destination:" + dest);
				sendNow(dest.getAddress(), Constants.SDN_PACKAGE, pkg);
			}
		}
	}
	
	public Map<String, Integer> getVmNameIdTable() {
		return this.vmNameIdTable;
	}
	
	public Map<String, Integer> getFlowNameIdTable() {
		return this.flowNameIdTable;
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
			if(ch.adjustDedicatedBandwidthAlongLink()) {
				// Channel BW is changed. send event.
			}
		}
		
		for(Channel ch:this.channelTable.values()) {
			if(ch.adjustSharedBandwidthAlongLink()) {
				// Channel BW is changed. send event.
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
		
		Channel channel = new Channel(chName, flowId, src, dst, nodes, links, reqBw, debugVmIdName.get(src), debugVmIdName.get(dst));

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

	public List<Switch> getSwitchList() {
		return this.switches;
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
	
	protected Switch createSwitch(SwitchSpec switchSpec){
		Switch sw = null;
		
		switch(switchSpec.getType()){
		case "core":
			sw = new CoreSwitch(switchSpec.getName(), switchSpec.getBw(), switchSpec.getIops(), switchSpec.getUpports(), switchSpec.getDownports(), this);
			break;
		case "aggregate":
			sw = new AggregationSwitch(switchSpec.getName(), switchSpec.getBw(), switchSpec.getIops(), switchSpec.getUpports(), switchSpec.getDownports(), this);
			break;
		case "edge":
			sw = new EdgeSwitch(switchSpec.getName(), switchSpec.getBw(), switchSpec.getIops(), switchSpec.getUpports(), switchSpec.getDownports(), this);
			break;
		default:
			System.err.println("No switch found!");
			//throw new IllegalArgumentException("No switch found!");
		}
		
		return sw;
	}
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (JsonIOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(HostSpec hostSpec : pdc.getHosts()){
			
			if(hostSpec.getNums() == 0){
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
				
				for(int n = 0 ; n < hostSpec.getNums() ; n++){
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
		
		for(SwitchSpec switchSpec : pdc.getSwitches()){
			if(switchSpec.getUpports() == 0){
				switchSpec.setUpports(MAX_PORTS);
			}
			
			if(switchSpec.getDownports() == 0){
				switchSpec.setDownports(MAX_PORTS);
			}
			
			Switch sw = createSwitch(switchSpec);
			
			if(sw != null) {
				nameIdTable.put(switchSpec.getName(), sw.getAddress());
				topology.addNode(sw);
				this.switches.add(sw);
			}
		}	
			
		for(LinkSpec linkSpec : pdc.getLinks()){
			
			int srcAddress = nameIdTable.get(linkSpec.getSource());
			int dstAddress = nameIdTable.get(linkSpec.getDestination());
			
			topology.addLink(srcAddress, dstAddress, linkSpec.getLatency());
		}
				
		topology.buildDefaultRouting();
		
		// Initialize the Embedder to get ready to serve VDC requests.
		embedder.init(topology);
	}
	
	// Used to assign flow Ids to Arcs.
	// TOCHECK: whether it should necessarily be static.
	private static int flowNumbers = 0;
	
	public boolean deployApplication(int userId, String vmsFileName){

		int datacenterId = brokerIdToDatacenterIdMap.get(userId);
		String datacenterName = datacenterIdToDatacenterMap.get(datacenterId).getName();
		
		VirtualTopology virtualTopology = new VirtualTopology(datacenterId, datacenterName);
		
		LinkedList<Middlebox> mbList = new LinkedList<Middlebox>();
		
		Gson gson = new Gson();
		VdcSpec vdc = null;
		
		try {
			vdc = gson.fromJson(new FileReader(vmsFileName), VdcSpec.class);
		} 
		catch (JsonSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (JsonIOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(VmSpec vmSpec : vdc.getVms()){
			
			if(vmSpec.getBw() == 0){
				vmSpec.setBw(1000);
			}
			
			if(vmSpec.getEndtime() == 0){
				vmSpec.setEndtime(Double.POSITIVE_INFINITY);
			}
			
			if(vmSpec.getNums() == 0){
				Vm vm = new TimedVm(vmId, vmSpec, userId, datacenterId, "VMM", new CloudletSchedulerTimeShared());
				
				String nodeName2 = vmSpec.getName();
				
				vmNameIdTable.put(nodeName2, vmId);
				NetworkOperatingSystem.debugVmIdName.put(vmId, nodeName2);
				vmList.add(vm);
				virtualTopology.addVm(vm);
				vmId++;
			}
			else{
				String nodeName = vmSpec.getName();
				String nodeName2 = nodeName;
				for(int n = 0 ; n < vmSpec.getNums() ; n++){
					nodeName2 = nodeName + n;
					
					Vm vm = new TimedVm(vmId, nodeName2, vmSpec, userId, datacenterId, "VMM", new CloudletSchedulerTimeShared());
					
					vmNameIdTable.put(nodeName2, vmId);
					NetworkOperatingSystem.debugVmIdName.put(vmId, nodeName2);
					vmList.add(vm);
					virtualTopology.addVm(vm);
					vmId++;
				}
			}
		}		
		
		for(VLinkSpec vLinkSpec : vdc.getLinks()){
			
			int srcId = vmNameIdTable.get(vLinkSpec.getSource());
			int dstId = vmNameIdTable.get(vLinkSpec.getDestination());
			
			int flowId = -1;
			
			if(vLinkSpec.getName() == null || "default".equalsIgnoreCase(vLinkSpec.getName())) {
				// Default flow.
				flowId = -1;
			}
			else {
				flowId = flowNumbers++;
				flowNameIdTable.put(vLinkSpec.getName(), flowId);
			}
			
			Arc arc = new Arc(vLinkSpec, srcId, dstId, flowId);
			
			arcList.add(arc);
			
			if(flowId != -1) {
					flowIdArcTable.put(flowId, arc);
			}
		}
		
		virtualTopologies.add(virtualTopology);
		
		boolean result = deployApplication(vmList, mbList, arcList, virtualTopology);
		
		if (result){
			isApplicationDeployed = true;
			return true;
		}
		
		return false;
	}
	
}
