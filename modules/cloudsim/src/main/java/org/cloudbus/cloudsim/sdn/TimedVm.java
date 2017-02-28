/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.sdn;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.FullVmStateHistoryEntry;
import org.cloudbus.cloudsim.Vm;

/**
 * Extension of VM that supports to set start and terminate time of VM in VM creation request.
 * If start time and finish time is set up, specific CloudSim Event is triggered
 * in datacenter to create and terminate the VM. 
 * 
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */
public class TimedVm extends Vm {

	private double startTime;
	private double finishTime;
	
	private String name;
	
	private int datacenterId;
	
	public TimedVm(String name, int id, int userId, int datacenterId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		this.name = name;
		this.datacenterId = datacenterId;
	}
	
	public TimedVm(String name, int id, int userId, int datacenterId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler, 
			double startTime, double finishTime) {
		
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		this.name = name;
		this.datacenterId = datacenterId;
		this.startTime = startTime;
		this.finishTime = finishTime;
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
		stateHistory.setBwUtil(getCloudletScheduler().getCurrentRequestedUtilizationOfBw());
		
		/*
		 * State History stored for the given time instant
		 */
		getFullVmStateHistory().add(stateHistory);
//		System.out.println("VM " + getId() + " state stored at time " + time);
	}
	
}
