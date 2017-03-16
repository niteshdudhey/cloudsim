/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.sdn;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.FullVmStateHistoryEntry;
import org.cloudbus.cloudsim.Vm;
import org.hamcrest.core.IsInstanceOf;

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
	
	private double currentUpBW;
	
	private double currentDownBW;
	
	private double mipsPerSendBW;
	
	private double mipsPerRecvBW;
	
	public TimedVm(String name, int id, int userId, int datacenterId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		this.name = name;
		this.datacenterId = datacenterId;
		currentUpBW = 0;
		currentDownBW = 0;
		mipsPerSendBW = 0.0;
		mipsPerRecvBW = 0.0;
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
		mipsPerSendBW = 0.0;
		mipsPerRecvBW = 0.0;
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
	
	public double getMipsPerSendBW() {
		return mipsPerSendBW;
	}
	
	public double getMipsPerRecvBW() {
		return mipsPerRecvBW;
	}
	
	public void setMipsPerSendBW(double mips) {
		mipsPerSendBW = mips;
		return;
	}
	
	public void setMipsPerRecvBW(double mips) {
		mipsPerRecvBW = mips;
		return;
	}
	
	public double updateSendCPU(double oldSendBW, double sendBW) {
		if (getCloudletScheduler() instanceof CloudletSchedulerTimeShared) {
			CloudletSchedulerTimeShared scheduler = (CloudletSchedulerTimeShared) getCloudletScheduler();
			double sendCPU = scheduler.getSendCPU();
			if (scheduler.getProcessingCPU() > sendBW*mipsPerSendBW) {
				scheduler.setSendCPU(scheduler.getSendCPU() + sendBW*mipsPerSendBW - oldSendBW*mipsPerSendBW, getDatacenterId());
				return sendBW;
			}
			else {
				scheduler.setSendCPU(scheduler.getSendCPU() + scheduler.getProcessingCPU() - oldSendBW*mipsPerSendBW, getDatacenterId());
				return scheduler.getProcessingCPU();
			}
		}
		else {
			System.err.println("TimedVm.updateSendCPU: Only implemented for Time Shared Cloudlet Scheduler");
			return sendBW;
		}
	}
	
	public double updateRecvCPU(double oldRecvBW, double recvBW) {
		if (getCloudletScheduler() instanceof CloudletSchedulerTimeShared) {
			CloudletSchedulerTimeShared scheduler = (CloudletSchedulerTimeShared) getCloudletScheduler();
			double recvCPU = scheduler.getRecvCPU();
			if (scheduler.getProcessingCPU() > recvBW*mipsPerRecvBW) {
				scheduler.setRecvCPU(scheduler.getRecvCPU() + recvBW*mipsPerRecvBW - oldRecvBW*mipsPerRecvBW, getDatacenterId());
				return recvBW;
			}
			else {
				scheduler.setRecvCPU(scheduler.getRecvCPU() + scheduler.getProcessingCPU() - oldRecvBW*mipsPerSendBW, getDatacenterId());
				return scheduler.getProcessingCPU();
			}
		}
		else {
			System.err.println("TimedVm.updateSendCPU: Only implemented for Time Shared Cloudlet Scheduler");
			return recvBW;
		}
	}
	
	//TODO: errors
	public double getPossibleSendBWFromCPU() {
		CloudletSchedulerTimeShared scheduler = (CloudletSchedulerTimeShared) getCloudletScheduler();
		return scheduler.getProcessingCPU()/mipsPerSendBW;
	}
	
	public double getPossibleRecvBWFromCPU() {
		CloudletSchedulerTimeShared scheduler = (CloudletSchedulerTimeShared) getCloudletScheduler();
		return scheduler.getProcessingCPU()/mipsPerRecvBW;
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
	
}
