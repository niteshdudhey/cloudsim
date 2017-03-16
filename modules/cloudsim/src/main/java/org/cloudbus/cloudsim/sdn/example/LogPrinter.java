/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn.example;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.FullHostStateHistoryEntry;
import org.cloudbus.cloudsim.FullVmStateHistoryEntry;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.sdn.Activity;
import org.cloudbus.cloudsim.sdn.Processing;
import org.cloudbus.cloudsim.sdn.Request;
import org.cloudbus.cloudsim.sdn.SDNHost;
import org.cloudbus.cloudsim.sdn.Switch;
import org.cloudbus.cloudsim.sdn.Transmission;
import org.cloudbus.cloudsim.sdn.VSwitch;
import org.cloudbus.cloudsim.sdn.VSwitchStateHistoryEntry;
import org.cloudbus.cloudsim.sdn.Switch.HistoryEntry;
import org.cloudbus.cloudsim.sdn.SwitchStateHistoryEntry;
import org.cloudbus.cloudsim.sdn.power.PowerUtilizationHistoryEntry;
import org.cloudbus.cloudsim.sdn.power.PowerUtilizationInterface;
import com.google.common.collect.Table;
import org.cloudbus.cloudsim.sdn.Link;
import org.cloudbus.cloudsim.sdn.LinkStateHistoryEntry;

/**
 * This class is to print out logs into console.
 * 
 * @author Jungmin Son
 * @since CloudSimSDN 1.0
 */
public class LogPrinter {
	
	/*
	 * Folder Name where all metrics summary files will be stored.
	 * Give the folder path including the slash at the end.
	 */
	public static String folderName = "/home/ravi/Documents/Ravi Teja A.V/RnD/";
	
	/*
	 * Folder Name where all the data files needed to generate plots will be stored.
	 * Give the folder path including the slash at the end.
	 */
	public static String dataFilesFolderName = "/home/ravi/Documents/Ravi Teja A.V/RnD/data_files/";
	
	public static void printEnergyConsumption(List<Host> hostList, List<Switch> switchList, double finishTime) {
		double hostEnergyConsumption = 0, switchEnergyConsumption = 0;
		
		Log.printLine("========== HOST POWER CONSUMPTION AND DETAILED UTILIZATION ===========");
		for(Host host:hostList) {
			PowerUtilizationInterface scheduler =  (PowerUtilizationInterface) host.getVmScheduler();
			scheduler.addUtilizationEntryTermination(finishTime);
			
			double energy = scheduler.getUtilizationEnergyConsumption();
			Log.printLine("Host #"+host.getId()+": "+energy);
			hostEnergyConsumption+= energy;

			printHostUtilizationHistory(scheduler.getUtilizationHisotry());

		}

		Log.printLine("========== SWITCH POWER CONSUMPTION AND DETAILED UTILIZATION ===========");
		for(Switch sw:switchList) {
			sw.addUtilizationEntryTermination(finishTime);
			double energy = sw.getUtilizationEnergyConsumption();
			Log.printLine("Switch #"+sw.getId()+": "+energy);
			switchEnergyConsumption+= energy;

			printSwitchUtilizationHistory(sw.getUtilizationHisotry());

		}
		Log.printLine("========== TOTAL POWER CONSUMPTION ===========");
		Log.printLine("Host energy consumed: "+hostEnergyConsumption);
		Log.printLine("Switch energy consumed: "+switchEnergyConsumption);
		Log.printLine("Total energy consumed: "+(hostEnergyConsumption+switchEnergyConsumption));
		
	}

	private static void printHostUtilizationHistory(
			List<PowerUtilizationHistoryEntry> utilizationHisotry) {
		if(utilizationHisotry != null)
			for(PowerUtilizationHistoryEntry h:utilizationHisotry) {
				Log.printLine(h.startTime+", "+h.usedMips);
			}
	}
	private static void printSwitchUtilizationHistory(List<HistoryEntry> utilizationHisotry) {
		if(utilizationHisotry != null)
			for(HistoryEntry h:utilizationHisotry) {
				Log.printLine(h.startTime+", "+h.numActivePorts);
			}
	}
	
	public static void printLinkUtilizationHistory(Table<Integer, Integer, Link> links){
		Collection<Link> linksList = links.values();			
		Set<Link> linksSet = new HashSet<Link>(linksList);
		
		Log.printLine("========== LINK UTILIZATIONS ===========");
		
		for(Link link : linksSet){
			Log.printLine("link: " + link.getName());
			Log.printLine("Time AvailableBw");
			List<LinkStateHistoryEntry> stateHistory = link.stateHistory;
			
			for(LinkStateHistoryEntry entry : stateHistory){
				Log.printLine(String.format("%.12f %.0f", entry.getTime(), entry.getAvailableBw()));
			}
		}
	}
	
	static public String indent = ",";
	static public String tabSize = "10";
	static public String fString = 	"%"+tabSize+"s"+indent;
	static public String fInt = 	"%"+tabSize+"d"+indent;
	static public String fFloat = 	"%"+tabSize+".3f"+indent;
	
	public static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		
		Log.print(String.format(fString, "Cloudlet_ID"));
		Log.print(String.format(fString, "STATUS" ));
		Log.print(String.format(fString, "DataCenter_ID"));
		Log.print(String.format(fString, "VM_ID"));
		Log.print(String.format(fString, "Length"));
		Log.print(String.format(fString, "Time"));
		Log.print(String.format(fString, "Start Time"));
		Log.print(String.format(fString, "Finish Time"));
		Log.print("\n");

		//DecimalFormat dft = new DecimalFormat("######.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			printCloudlet(cloudlet);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void printCloudlet(Cloudlet cloudlet) {
		Log.print(String.format(fInt, cloudlet.getCloudletId()));

		if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
			Log.print(String.format(fString, "SUCCESS"));
			Log.print(String.format(fInt, cloudlet.getResourceId()));
			Log.print(String.format(fInt, cloudlet.getVmId()));
			Log.print(String.format(fInt, cloudlet.getCloudletLength()));
			Log.print(String.format(fFloat, cloudlet.getActualCPUTime()));
			Log.print(String.format(fFloat, cloudlet.getExecStartTime()));
			Log.print(String.format(fFloat, cloudlet.getFinishTime()));
			Log.print("\n");
		}
		else {
			//Log.printLine("FAILED");
			Log.print("FAILED");
			Log.print(cloudlet.getCloudletStatusString());
			Log.print(String.format(fInt, cloudlet.getResourceId()));
			Log.print(String.format(fInt, cloudlet.getVmId()));
			Log.print(String.format(fInt, cloudlet.getCloudletLength()));
			Log.print(String.format(fFloat, cloudlet.getActualCPUTime()));
			Log.print(String.format(fFloat, cloudlet.getExecStartTime()));
			Log.print(String.format(fFloat, cloudlet.getFinishTime()));
			Log.print("\n");
		}
	}
	
	private static double startTime, finishTime;
	public static void printWorkloadList(List<Workload> wls) {
		int[] appIdNum = new int[SDNBroker.appId];
		double[] appIdTime = new double[SDNBroker.appId];
		double[] appIdStartTime = new double[SDNBroker.appId];
		double[] appIdFinishTime = new double[SDNBroker.appId];
		
		double serveTime, totalTime = 0;

		Log.printLine();
		Log.printLine("========== DETAILED RESPONSE TIME OF WORKLOADS ===========");

		if(wls.size() == 0) return;
		
		Log.print(String.format(fString, "App_ID"));
		printRequestTitle(wls.get(0).request);
		Log.print(String.format(fString, "ResponseTime"));
		Log.printLine();

		for(Workload wl:wls) {
			Log.print(String.format(fInt, wl.appId));
			
			startTime = finishTime = -1;
			printRequest(wl.request);
			
			serveTime= (finishTime - startTime);
			Log.print(String.format(fFloat, serveTime));
			totalTime += serveTime;
			
			appIdNum[wl.appId] ++;	//How many workloads in this app.
			appIdTime[wl.appId] += serveTime;
			if(appIdStartTime[wl.appId] <=0) {
				appIdStartTime[wl.appId] = wl.time;
			}
			appIdFinishTime[wl.appId] = wl.time;
			Log.printLine();
		}

		Log.printLine("========== AVERAGE RESULT OF WORKLOADS ===========");
		for(int i=0; i<SDNBroker.appId; i++) {
			Log.printLine("App Id ("+i+"): "+appIdNum[i]+" requests, Start=" + appIdStartTime[i]+
					", Finish="+appIdFinishTime[i]+", Rate="+(double)appIdNum[i]/(appIdFinishTime[i] - appIdStartTime[i])+
					" req/sec, Response time=" + appIdTime[i]/appIdNum[i]);
		}
		
		//printGroupStatistics(WORKLOAD_GROUP_PRIORITY, appIdNum, appIdTime);
		
		Log.printLine("Average Response Time:"+(totalTime / wls.size()));
		
	}

	private static void printRequestTitle(Request req) {
		//Log.print(String.format(fString, "Req_ID"));
		//Log.print(String.format(fFloat, req.getStartTime()));
		//Log.print(String.format(fFloat, req.getFinishTime()));
		
		List<Activity> acts = req.getRemovedActivities();
		for(Activity act:acts) {
			if(act instanceof Transmission) {
				Transmission tr=(Transmission)act;
				Log.print(String.format(fString, "Tr:Size"));
				Log.print(String.format(fString, "Tr:Channel"));
				
				Log.print(String.format(fString, "Tr:time"));
				Log.print(String.format(fString, "Tr:Start"));
				Log.print(String.format(fString, "Tr:End"));
				
				printRequestTitle(tr.getPackage().getPayload());
			}
			else {
				Log.print(String.format(fString, "Pr:Size"));
				
				Log.print(String.format(fString, "Pr:time"));
				Log.print(String.format(fString, "Pr:Start"));
				Log.print(String.format(fString, "Pr:End"));
			}
		}
	}
	
	private static void printRequest(Request req) {
		//Log.print(String.format(fInt, req.getRequestId()));
		//Log.print(String.format(fFloat, req.getStartTime()));
		//Log.print(String.format(fFloat, req.getFinishTime()));
		
		List<Activity> acts = req.getRemovedActivities();
		for(Activity act:acts) {
			if(act instanceof Transmission) {
				Transmission tr=(Transmission)act;
				Log.print(String.format(fInt, tr.getPackage().getSize()));
				Log.print(String.format(fInt, tr.getPackage().getFlowId()));
				
				Log.print(String.format(fFloat, tr.getPackage().getFinishTime() - tr.getPackage().getStartTime()));
				Log.print(String.format(fFloat, tr.getPackage().getStartTime()));
				Log.print(String.format(fFloat, tr.getPackage().getFinishTime()));
				
				printRequest(tr.getPackage().getPayload());
			}
			else {
				Processing pr=(Processing)act;
				Log.print(String.format(fInt, pr.getCloudlet().getCloudletLength()));

				Log.print(String.format(fFloat, pr.getCloudlet().getActualCPUTime()));
				Log.print(String.format(fFloat, pr.getCloudlet().getExecStartTime()));
				Log.print(String.format(fFloat, pr.getCloudlet().getFinishTime()));

				if(startTime == -1) startTime = pr.getCloudlet().getExecStartTime();
				finishTime=pr.getCloudlet().getFinishTime();
			}
		}
	}
	
	public static void printGroupStatistics(int groupSeperateNum, int[] appIdNum, double[] appIdTime) {

		double prioritySum = 0, standardSum = 0;
		int priorityReqNum = 0, standardReqNum =0;
		
		for(int i=0; i<SDNBroker.appId; i++) {
			double avgResponseTime = appIdTime[i]/appIdNum[i];
			if(i<groupSeperateNum) {
				prioritySum += avgResponseTime;
				priorityReqNum += appIdNum[i];
			}
			else {
				standardSum += avgResponseTime;
				standardReqNum += appIdNum[i];
			}
		}

		Log.printLine("Average Response Time(Priority):"+(prioritySum / priorityReqNum));
		Log.printLine("Average Response Time(Standard):"+(standardSum / standardReqNum));
	}
	
	public static void printHostMetricsToFile(List<SDNHost> hostList) {
		String fileName = folderName + "metrics_host.txt";
		try {
			File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            for (SDNHost host: hostList) {
    			fw.write("Host " + host.getName() + "\n");
    			fw.write("-----------\n");
    			for (FullHostStateHistoryEntry entry : host.getFullHostStateHistory()) {
                    fw.write("Time = " + entry.getTime() + "\n" + entry.toString() + "\n");
                    addToDataFile("Host-" + host.getName() + "-RAM.dat", entry.getTime(), entry.getRam());
                    addToDataFile("Host-" + host.getName() + "-Available-RAM.dat", entry.getTime(), entry.getAvailableRam());
                    addToDataFile("Host-" + host.getName() + "-Requested-RAM.dat", entry.getTime(), entry.getRequestedRam());
                    addToDataFile("Host-" + host.getName() + "-BW.dat", entry.getTime(), entry.getBw());
                    addToDataFile("Host-" + host.getName() + "-Available-BW.dat", entry.getTime(), entry.getAvailableBw());
                    addToDataFile("Host-" + host.getName() + "-Requested-BW.dat", entry.getTime(), entry.getRequestedBw());
                    addToDataFile("Host-" + host.getName() + "-MIPS.dat", entry.getTime(), entry.getRam());
                    addToDataFile("Host-" + host.getName() + "-Available-MIPS.dat", entry.getTime(), entry.getAvailableMips());
                    addToDataFile("Host-" + host.getName() + "-Requested-MIPS.dat", entry.getTime(), entry.getRequestedMips());
                    addToDataFile("Host-" + host.getName() + "-RAM-Util.dat", entry.getTime(), entry.getRamUtil());
//                    addToDataFile("Host-" + host.getName() + "-BW-Util.dat", entry.getTime(), entry.getBwUtil());
                    addToDataFile("Host-" + host.getName() + "-Up-BW-Util.dat", entry.getTime(), entry.getUpBwUtil());
                    addToDataFile("Host-" + host.getName() + "-Down-BW-Util.dat", entry.getTime(), entry.getDownBwUtil());
                    addToDataFile("Host-" + host.getName() + "-CPU-Util.dat", entry.getTime(), entry.getCpuUtil());
                }
    		}
            fw.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printVmMetricsToFile(List<? extends Vm> vmList) {
		String fileName = folderName + "metrics_vm.txt";
		try {
			File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            for (Vm vm: vmList) {
    			fw.write("VM " + vm.getId() + "\n");
    			fw.write("----\n");
    			for (FullVmStateHistoryEntry entry : vm.getFullVmStateHistory()) {
    				fw.write("Time = " + entry.getTime() + "\n" + entry.toString() + "\n");
    				addToDataFile("VM-" + vm.getId() + "-Available-RAM.dat", entry.getTime(), entry.getAllocatedRam());
                    addToDataFile("VM-" + vm.getId() + "-Requested-RAM.dat", entry.getTime(), entry.getRequestedRam());
                    addToDataFile("VM-" + vm.getId() + "-Available-BW.dat", entry.getTime(), entry.getAllocatedBw());
                    addToDataFile("VM-" + vm.getId() + "-Requested-BW.dat", entry.getTime(), entry.getRequestedBw());
                    addToDataFile("VM-" + vm.getId() + "-Available-MIPS.dat", entry.getTime(), entry.getAllocatedMips());
                    addToDataFile("VM-" + vm.getId() + "-Requested-MIPS.dat", entry.getTime(), entry.getRequestedMips());
                    addToDataFile("VM-" + vm.getId() + "-RAM-Util.dat", entry.getTime(), entry.getRamUtil());
//                    addToDataFile("VM-" + vm.getId() + "-BW-Util.dat", entry.getTime(), entry.getBwUtil());
                    addToDataFile("VM-" + vm.getId() + "-Up-BW-Util.dat", entry.getTime(), entry.getUpBwUtil());
                    addToDataFile("VM-" + vm.getId() + "-Down-BW-Util.dat", entry.getTime(), entry.getDownBwUtil());
                    addToDataFile("VM-" + vm.getId() + "-CPU-Util.dat", entry.getTime(), entry.getCpuUtil());
    			}
    		}
            fw.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printLinkMetricsToFile(Table<Integer, Integer, Link> links){
		Collection<Link> linksList = links.values();			
		Set<Link> linksSet = new HashSet<Link>(linksList);
		
		String fileName = folderName + "metrics_link.txt";
		try {
			File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            for(Link link : linksSet){
            	fw.write("Link " + link.getName() + "\n");
    			fw.write("---------------\n");
    			List<LinkStateHistoryEntry> stateHistory = link.stateHistory;
    			for(LinkStateHistoryEntry entry : stateHistory){
    				fw.write("Time = " + entry.getTime() + "\n" + entry.toString() + "\n");
    				addToDataFile("Link-" + link.getName() + "-Available-BW.dat", entry.getTime(), entry.getAvailableBw());
    			}
    		}
            fw.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printSwitchMetricsToFile(List<Switch> switchList) {
		String fileName = folderName + "metrics_switch.txt";
		try {
			File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            for (Switch pswitch: switchList) {
    			fw.write("Switch " + pswitch.getName() + "\n");
    			fw.write("--------\n");
    			for (SwitchStateHistoryEntry entry : pswitch.getSwitchStateHistory()) {
    				fw.write("Time = " + entry.getTime() + "\n" + entry.toString() + "\n");
    				addToDataFile("Switch-" + pswitch.getName() + "-Num-Packets.dat", entry.getTime(), entry.getPacketsTransferred());                    
    			}
    		}
            fw.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printVSwitchMetricsToFile(List<VSwitch> vswitchList) {
		String fileName = folderName + "metrics_vswitch.txt";
		try {
			File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            for (VSwitch vswitch: vswitchList) {
    			fw.write("VSwitch " + vswitch.getName() + "\n");
    			fw.write("--------\n");
    			for (VSwitchStateHistoryEntry entry : vswitch.getVSwitchStateHistory()) {
    				fw.write("Time = " + entry.getTime() + "\n" + entry.toString() + "\n");
    				addToDataFile("VSwitch-" + vswitch.getName() + "-Num-Packets.dat", entry.getTime(), entry.getPacketsTransferred());                    
    			}
    		}
            fw.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public static void addToDataFile(String header, double time, double value) {
		String fileName = dataFilesFolderName + header;
		try {
			File file = new File(fileName);
			int nocreate = 0;
            if (!file.exists()) {
                file.createNewFile();
                nocreate = 1;
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            if (nocreate == 1) {
            	fw.write("Time, " + header + "\n");
            }
            fw.write(Double.toString(time) + ", " + Double.toString(value) + "\n");
            fw.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	
}
