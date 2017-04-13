/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.sdn;

import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.FullVmStateHistoryEntry;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VmSpec;

/**
 * Extension of VM that supports to set start and terminate time of VM in VM creation request.
 * If start time and finish time is set up, specific CloudSim Event is triggered
 * in datacenter to create and terminate the VM. 
 * 
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */

/**
 * The TimedVm could later be changed to a SimEntity and then all the nodes in the both Physical
 * and Virtual Networks will be derived from class Node and Nod will be a SimEntity.
 * 
 * @author Nitesh Dudhey
 *
 */
public class TimedVm extends Vm implements VNode {

	private int rank = 3;
	
	private double startTime;
	
	private double finishTime;
	
	private String name;
	
	private int datacenterId;
	
	/**
	 * The candidate host on which this VM could be hosted.
	 */
	// Not using the Host member of the Vm class because it represents 
	// the host after the allocation of Vm.
	private SDNHost candidateHost;
	
	private double currentUpBW;
	
	private double currentDownBW;
	
	private boolean active;
	
	private List<VNode> upperVNodes;
	
	public TimedVm(String name, int id, int userId, int datacenterId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		this.name = name;
		this.datacenterId = datacenterId;
		currentUpBW = 0;
		currentDownBW = 0;
		this.active = false;		
		this.upperVNodes = new ArrayList<VNode>();
	}
	
	public TimedVm(String name, int id, int userId, int datacenterId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler, 
			double startTime, double finishTime) {
		
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		
		this.name = name;
		this.datacenterId = datacenterId;
		this.startTime = startTime;
		this.finishTime = finishTime;
		currentUpBW = 0;
		currentDownBW = 0;
		this.active = false;
		this.upperVNodes = new ArrayList<VNode>();
	}
	
	public TimedVm(int id, VmSpec vmSpec, int userId, int datacenterId, String vmm, CloudletScheduler cloudletScheduler) {
		
		super(id, userId, vmSpec.getMips(), vmSpec.getPes(), vmSpec.getRam(), vmSpec.getBw(), vmSpec.getSize(), vmm, cloudletScheduler);
		
		this.name = vmSpec.getName();
		this.datacenterId = datacenterId;
		this.startTime = vmSpec.getStarttime();
		this.finishTime = vmSpec.getEndtime();
		this.active = false;
		this.upperVNodes = new ArrayList<VNode>();
	}
	
	public TimedVm(int id, String name, VmSpec vmSpec, int userId, int datacenterId, String vmm, CloudletScheduler cloudletScheduler) {
		
		super(id, userId, vmSpec.getMips(), vmSpec.getPes(), vmSpec.getRam(), vmSpec.getBw(), vmSpec.getSize(), vmm, cloudletScheduler);
		
		this.name = name;
		this.datacenterId = datacenterId;
		this.startTime = vmSpec.getStarttime();
		this.finishTime = vmSpec.getEndtime();
		this.active = false;
		this.upperVNodes = new ArrayList<VNode>();
	}
	
	public String getName() {
		return name;
	}
	
	// The id of the datacenter in which the VM is located.
	public int getDatacenterId() {
		return datacenterId;
	}

	public double getStartTime() {
		return startTime;
	}
	
	public double getFinishTime() {
		return finishTime;
	}
	
	public double getCurrentUpBW() {
		return currentUpBW;
	}
	
	private void setCurrentUpBW(double bw) {
		currentUpBW = bw;
		return;
	}
	
	public void incrementCurrentUpBW(double diff) {
		if (getCurrentUpBW()+diff>=0) {
			setCurrentUpBW(getCurrentUpBW()+diff);
		}
		else {
			//TODO: warn
			setCurrentUpBW(0);
		}
		return;
	}
	
	public double getCurrentDownBW() {
		return currentDownBW;
	}
	
	private void setCurrentDownBW(double bw) {
		currentDownBW = bw;
		return;
	}
	
	public void incrementCurrentDownBW(double diff) {
		if (getCurrentDownBW()+diff>=0) {
			setCurrentDownBW(getCurrentDownBW()+diff);
		}
		else {
			//TODO: warn
			setCurrentDownBW(0);
		}
		return;
	}
	
	public List<VNode> getLowerVNodes() {
		return null;
	}
	
	public List<VNode> getUpperVNodes() {
		return upperVNodes;
	}
	
	public void addLowerVNode(VNode lowerNode) {
		System.err.println("Vm cannot have a lower vnode.");
	}
	
	public void addUpperVNode(VNode upperNode) {
		if (upperNode == null) {
			System.err.println("Vm cannot have null upper vnode.");
		} else {
			this.upperVNodes.add(upperNode);
		}
	}
	
	@Override
	public void storeCurrentState(double time) {
		
		FullVmStateHistoryEntry stateHistory = new FullVmStateHistoryEntry(time);
		
		double totalAllocatedMips = 0.0;
		double totalRequestedMips = 0.0;
		if (getCurrentAllocatedMips() != null) {
			for (double mips: getCurrentAllocatedMips()) {
				totalAllocatedMips += mips;
			}
		}
		if (getCurrentRequestedMips() != null) {
			for (double mips: getCurrentRequestedMips()) {
				totalRequestedMips += mips;
			}
		}
		
		/*
		 * Add all function calls to setters of StateHistory attributes
		 */
		
		stateHistory.setAllocatedRam(getCurrentAllocatedRam());
		stateHistory.setRequestedRam(getRam());
		
		stateHistory.setAllocatedBw(getCurrentAllocatedBw());
		stateHistory.setRequestedBw(getBw());
		
		stateHistory.setAllocatedMips(totalAllocatedMips);
		stateHistory.setAllocatedMipsList(getCurrentAllocatedMips());
		stateHistory.setRequestedMips(totalRequestedMips);
		
		stateHistory.setRamUtil(getCloudletScheduler().getCurrentRequestedUtilizationOfRam());
		stateHistory.setCpuUtil(getCloudletScheduler().getTotalUtilizationOfCpu(time));
		
		// TODO
		stateHistory.setUpBwUtil(getCurrentUpBW()/getCurrentAllocatedBw());
		stateHistory.setDownBwUtil(getCurrentDownBW()/getCurrentAllocatedBw());
		stateHistory.setBwUtil(getCloudletScheduler().getCurrentRequestedUtilizationOfBw());
		
		/*
		 * State History stored for the given time instant
		 */
		getFullVmStateHistory().add(stateHistory);
//		System.out.println("VM " + getId() + " state stored at time " + time);
	}
	
	public SDNHost getCandidateHost() {
		return candidateHost;
	}

	public void setCandidateHost(SDNHost candidateHost) {
		this.candidateHost = candidateHost;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public String toString() {
		String str = getName() + ": " + getId();
		return str;
	}
	
}
