/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.sdn.example;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.sdn.Constants;
import org.cloudbus.cloudsim.sdn.SDNDatacenter;

/**
 * Broker class for CloudSimSDN example. This class represents a broker (Service Provider)
 * who uses the Cloud data center.
 * 
 * @author Jungmin Son
 * @since CloudSimSDN 1.0
 */

/**
 * Each Broker is associated with one Datacenter.
 * @author Nitesh Dudhey
 *
 */
public class SDNBroker extends SimEntity {
	
	private double requestedStartTime;
	
	private double requestedDuration;
	
	private double actualStartTime;
	
	private double finishTime;
	
	private double startTime;
	
	private double endTime;
	
	private boolean addedToQueue;

	private SDNDatacenter datacenter = null;
	
	private String applicationFileName = null;
	
	private List<String> workloadFileNames = null;

	private List<Cloudlet> cloudletList;
	
	private List<Workload> workloads;
	
	public SDNBroker(String name) throws Exception {
		super(name);
		this.workloadFileNames = new ArrayList<String>();
		this.cloudletList = new ArrayList<Cloudlet>();
		this.workloads = new ArrayList<Workload>();
		this.startTime = 0.0;
		this.endTime = Double.POSITIVE_INFINITY;
		this.requestedStartTime = 0.0;
		this.requestedDuration = Double.POSITIVE_INFINITY;
		this.actualStartTime = 0.0;
		this.finishTime = Double.POSITIVE_INFINITY;
		this.addedToQueue = false;
	}
	
	@Override
	public void startEntity() {
		System.out.println("Starting Broker:" + this.getId());
		// Replace the below line with the schedule() to schedule it to start at some different time.
		// This will enable the datacenter to come up at a given time.
		applicationSubmit();
	}
	
	@Override
	public void shutdownEntity() {
		List<Vm> vmList = this.datacenter.getVmList();
		
		for(Vm vm : vmList) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Shuttingdown.. VM:" + vm.getId());
		}
	}
	
	public void submitDeployApplication(SDNDatacenter dc, String filename) {
		this.datacenter = dc;
		this.applicationFileName = filename;
	}
	
	public void submitRequests(String filename) {
		this.workloadFileNames.add(filename);
	}

	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();
		
		switch(tag){
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			case Constants.APPLICATION_SUBMIT_ACK:
				// Application successfully submitted.
				// Now submitting workloads.
				applicationSubmitCompleted(); 
				break;
			case Constants.REQUEST_COMPLETED:
				requestCompleted(ev); 
				break;
			default: 
				System.out.println("Unknown event received by " + super.getName() + ". Tag:" + ev.getTag());
		}
	}
	
	private void applicationSubmit(){
		sendNow(this.datacenter.getId(), Constants.APPLICATION_SUBMIT, this.applicationFileName);
	}
	
	private void processVmCreate(SimEvent ev) {	
	}
	
	public void processVmCreateAckImmediate() {
		
	}
	
	private void requestCompleted(SimEvent ev) {
	}
	
	public List<Cloudlet> getCloudletReceivedList() {
		return cloudletList;
	}

	public static int appId = 0;
	
	private void applicationSubmitCompleted() {
		for (String workloadFileName : this.workloadFileNames) {
			scheduleRequest(workloadFileName);
			SDNBroker.appId++;
		}
	}
	
	private void scheduleRequest(String workloadFile) {
		WorkloadParser rp = new WorkloadParser(workloadFile, this.getId(), new UtilizationModelFull(),  
				this.datacenter.getVmNameIdTable(), this.datacenter.getFlowNameIdTable());
		
		for(Workload wl : rp.getWorkloads()) {
			send(this.datacenter.getId(), wl.time, Constants.REQUEST_SUBMIT, wl.request);
			wl.appId = SDNBroker.appId;
		}
		
		this.cloudletList.addAll(rp.getAllCloudlets());
		this.workloads.addAll(rp.getWorkloads());
	}
	
	public List<Workload> getWorkloads() {
		return this.workloads;
	}
	
	public double getRequestedStartTime() {
		return requestedStartTime;
	}

	public void setRequestedStartTime(double startTime) {
		this.requestedStartTime = startTime;
	}
	
	public double getActualStartTime() {
		return actualStartTime;
	}
	
	public void setActualStartTime(double startTime) {
		actualStartTime = startTime;
	}
	
	public double getFinishTime() {
		return finishTime;
	}
	
	public void setFinishTime(double time) {
		finishTime = time;
	}
	
	public double getRequestedDuration() {
		return requestedDuration;
	}

	public void setRequestedDuration(double duration) {
		this.requestedDuration = duration;
	}
	
	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}
	
	public boolean isAddedToQueue() {
		return addedToQueue;
	}
	
	public void setAddedToQueue() {
		addedToQueue = true;
	}

}
