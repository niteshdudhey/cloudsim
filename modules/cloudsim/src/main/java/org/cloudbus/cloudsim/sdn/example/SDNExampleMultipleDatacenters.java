package org.cloudbus.cloudsim.sdn.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.sdn.NetworkOperatingSystem;
import org.cloudbus.cloudsim.sdn.SDNDatacenter;
import org.cloudbus.cloudsim.sdn.VdcEmbedderSimple;
import org.cloudbus.cloudsim.sdn.VdcEmbedderSwitchLFF;
import org.cloudbus.cloudsim.sdn.Switch;
import org.cloudbus.cloudsim.sdn.VdcEmbedder;
import org.cloudbus.cloudsim.sdn.example.SDNExample.VmAllocationPolicyEnum;
import org.cloudbus.cloudsim.sdn.example.SDNExample.VmAllocationPolicyFactory;
import org.cloudbus.cloudsim.sdn.example.policies.VmAllocationPolicyCombinedLeastFullFirst;
import org.cloudbus.cloudsim.sdn.example.policies.VmAllocationPolicyCombinedMostFullFirst;
import org.cloudbus.cloudsim.sdn.example.policies.VmAllocationPolicyMipsLeastFullFirst;
import org.cloudbus.cloudsim.sdn.example.policies.VmAllocationPolicyMipsMostFullFirst;
import org.cloudbus.cloudsim.sdn.overbooking.OverbookingNetworkOperatingSystem;
import org.cloudbus.cloudsim.sdn.overbooking.VmAllocationPolicyOverbooking;
import org.cloudbus.cloudsim.sdn.power.PowerUtilizationMaxHostInterface;

public class SDNExampleMultipleDatacenters {
	protected static String physicalTopologyFile = "dataset-energy/energy-physical.json";
	
	// Virtual Datacenters File.
	protected static List<String> deploymentFiles = new ArrayList<String>();
	
	// Workloads for each Virtual Datacenter.
	protected static List<String> workloadFiles = new ArrayList<String>();
	
	protected static NetworkOperatingSystem nos;
	protected static PowerUtilizationMaxHostInterface maxHostHandler = null;
	
	// One broker for each VDC.
	private static List<SDNBroker> brokers = new ArrayList<SDNBroker>();
	
	private static boolean logEnabled = true;

	public interface VmAllocationPolicyFactory {
		public VmAllocationPolicy create(List<? extends Host> list);
	}
	
	enum VmAllocationPolicyEnum{ CombLFF, CombMFF, MipLFF, MipMFF, OverLFF, OverMFF, LFF, MFF, Overbooking}	
	
	private static void printUsage() {
		String runCmd = "java SDNExampleMultipleDataCenters";
		System.out.format("Usage: %s <LFF|MFF> [physical.json] [virtual1.json] [workload1.csv] [virtual2.json] [workload2.csv] ...\n", runCmd);
	}

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		// Parse system arguments.
		if(args.length < 1) {
			printUsage();
			System.exit(1);
		}
		
		VmAllocationPolicyEnum vmAllocPolicy = VmAllocationPolicyEnum.valueOf(args[0]);
		
		if(args.length > 1) {
			physicalTopologyFile = args[1];
		}
		
		if(args.length > 2) {
			for(int i = 2 ; i < args.length ; i += 2) {
				deploymentFiles.add(args[i]);
				workloadFiles.add(args[i + 1]);
			}
		}
		
		printArguments();
		Log.printLine("Starting CloudSim SDN...");

		try {
			// Initialize
			int num_user = 1;                               // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;                     // mean trace events
			CloudSim.init(num_user, calendar, trace_flag);
			
			VmAllocationPolicyFactory vmAllocationFac = null;
			NetworkOperatingSystem snos = null;
			
			VdcEmbedder embedder = new VdcEmbedderSwitchLFF();
			
			switch(vmAllocPolicy) {
			case CombMFF:
			case MFF:
				vmAllocationFac = new VmAllocationPolicyFactory() {
					public VmAllocationPolicy create(List<? extends Host> hostList) { return new VmAllocationPolicyCombinedMostFullFirst(hostList); }
				};
				snos = new SimpleNetworkOperatingSystem(physicalTopologyFile, embedder);
				break;
			case CombLFF:
			case LFF:
				vmAllocationFac = new VmAllocationPolicyFactory() {
					public VmAllocationPolicy create(List<? extends Host> hostList) { return new VmAllocationPolicyCombinedLeastFullFirst(hostList); }
				};
				snos = new SimpleNetworkOperatingSystem(physicalTopologyFile, embedder);
				break;
			case MipMFF:
				vmAllocationFac = new VmAllocationPolicyFactory() {
					public VmAllocationPolicy create(List<? extends Host> hostList) { return new VmAllocationPolicyMipsMostFullFirst(hostList); }
				};
				snos = new SimpleNetworkOperatingSystem(physicalTopologyFile, embedder);
				break;
			case MipLFF:
				vmAllocationFac = new VmAllocationPolicyFactory() {
					public VmAllocationPolicy create(List<? extends Host> hostList) { return new VmAllocationPolicyMipsLeastFullFirst(hostList); }
				};
				snos = new SimpleNetworkOperatingSystem(physicalTopologyFile, embedder);
				break;
			case Overbooking:
				vmAllocationFac = new VmAllocationPolicyFactory() {
					public VmAllocationPolicy create(List<? extends Host> hostList) { return new VmAllocationPolicyOverbooking(hostList); }
				};
				snos = new OverbookingNetworkOperatingSystem(physicalTopologyFile, embedder);
				break;
			default:
				System.err.println("Choose proper VM placement polilcy!");
				printUsage();
				System.exit(1);
			}
			
			nos = snos;
			
			// Initializing the deployment of various VDCs and their workloads.
			initializeDeploymentApplication(vmAllocationFac);
			
			if(!SDNExampleMultipleDatacenters.logEnabled) 
				Log.disable();
			
			// Starting simulations.
			double finishTime = CloudSim.startSimulation();
			CloudSim.stopSimulation();
			Log.enable();

			Log.printLine(finishTime + ": ========== EXPERIMENT FINISHED ===========");
			
			// Printing results after simulation is over.
			
			for(SDNBroker broker : brokers){
				List<Cloudlet> newList = broker.getCloudletReceivedList();
				
				if(SDNExampleMultipleDatacenters.logEnabled) {
					LogPrinter.printCloudletList(newList);
				}
				
				List<Workload> wls = broker.getWorkloads();
				LogPrinter.printWorkloadList(wls);
			}
						
			// Print hosts' and switches' total utilization.
			
			List<Host> hostList = nos.getHostList();
			List<Switch> switchList = nos.getSwitchList();
			LogPrinter.printEnergyConsumption(hostList, switchList, finishTime);
			
			printAllMetricsToFile();
			
			LogPrinter.printLinkUtilizationHistory(nos.getPhysicalTopology().getLinks());
			
			Log.printLine("Simultanously used hosts:" + maxHostHandler.getMaxNumHostsUsed());			
			Log.printLine("CloudSim SDN finished!");

		} 
		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	
	public static void initializeDeploymentApplication(VmAllocationPolicyFactory vmAllocationFac){
		// Create a Datacenter.
		SDNDatacenter datacenter = createSDNDatacenter(0, vmAllocationFac);
		
		for(int i = 0 ; i < deploymentFiles.size() ; i++){
			// Broker
			SDNBroker broker = createBroker(i);
			int brokerId = broker.getId();

			nos.addBroker(datacenter, brokerId, broker);
			
			System.out.println("Created broker with userId = " + brokerId);

			// Submit virtual topology.
			// deploymentFile : virtual-topology file.
			broker.submitDeployApplication(datacenter, deploymentFiles.get(i));
			
			// Submit workload for the VDC.
			submitWorkload(broker, workloadFiles.get(i));
			
			brokers.add(broker);
		}
	}
	
	public static void submitWorkload(SDNBroker broker, String workloadFile) {
		broker.submitRequests(workloadFile);
	}
	
	public static void printArguments() {
		System.out.println("Data center infrastructure (Physical Topology) : " + physicalTopologyFile);
		
		System.out.println("Virtual Networks (Virtual Topology) and Workloads : ");
		
		for(int i = 0 ; i < deploymentFiles.size() ; i++){
			System.out.println("Network:" + deploymentFiles.get(i) + " Workload:" + workloadFiles.get(i));
		}		
	}
	
	public static void printAllMetricsToFile() {
		File folder = new File(LogPrinter.dataFilesFolderName);
		File[] files = folder.listFiles();
	    for (int i = 0; i < files.length; i++) {
	    	if (files[i].isFile()) {
	    		files[i].delete();
	    	}
	    }
		LogPrinter.printHostMetricsToFile(nos.getSDNHostList());
		LogPrinter.printVmMetricsToFile(nos.getVmList());
		LogPrinter.printLinkMetricsToFile(nos.getPhysicalTopology().getLinks());
		LogPrinter.printSwitchMetricsToFile(nos.getSwitchList());
		LogPrinter.printVSwitchMetricsToFile(nos.getVSwitchList());
		LogPrinter.printVDCMetricsToFile(nos.getVDCRequestMetrics());
	}
	
	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	protected static SDNDatacenter createSDNDatacenter(int nameInt, 
			VmAllocationPolicyFactory vmAllocationFactory) {
		
		String name = "Datacenter_" + nameInt;
		
		List<Host> hostList = nos.getHostList();

		String arch = "x86";                                           // system architecture
		String os = "Linux";                                           // operating system
		String vmm = "Xen";
		
		double time_zone = 10.0;                                       // time zone this resource located
		double cost = 3.0;                                             // the cost of using processing in this resource
		double costPerMem = 0.05;                                      // the cost of using memory in this resource
		double costPerStorage = 0.001;                                 // the cost of using storage in this
										                               // resource
		double costPerBw = 0.0;                                        // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();   // we are not adding SAN devices by now.

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// Create Datacenter with previously set parameters.
		SDNDatacenter datacenter = null;
		try {
			VmAllocationPolicy vmPolicy = vmAllocationFactory.create(hostList);
			maxHostHandler = (PowerUtilizationMaxHostInterface) vmPolicy;
			datacenter = new SDNDatacenter(name, characteristics, vmPolicy, storageList, 0, nos);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return datacenter;
	}

	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	protected static SDNBroker createBroker(int id) {
		SDNBroker broker = null;
		
		try {
			broker = new SDNBroker("Broker_" + id);
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return broker;
	}
}
