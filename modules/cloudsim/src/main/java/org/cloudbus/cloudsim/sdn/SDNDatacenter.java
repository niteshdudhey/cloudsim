/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.sdn;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * Extended class of Datacenter that supports processing SDN-specific events.
 * In addtion to the default Datacenter, it processes Request submission to VM,
 * and application deployment request. 
 * 
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */

/**
 * Modified to add the support of start time.
 * 
 * @author Nitesh Dudhey
 *
 */
public class SDNDatacenter extends Datacenter {

	double startTime;
	
	double endTime;
	
	NetworkOperatingSystem nos;
	
	List<VSwitch> vswitchList;
	
	public SDNDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval, NetworkOperatingSystem nos) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		
		this.nos = nos;
		
		this.vswitchList = new LinkedList<VSwitch>();
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


	/**
	 * Adds a VM to the datacenter.
	 * @param vm
	 */
	public void addVm(Vm vm){
		getVmList().add(vm);
		
		if (vm.isBeingInstantiated()) {
			vm.setBeingInstantiated(false);
		}
		
		vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
	}
		
	@Override
	protected void processVmCreate(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		
		TimedVm tvm = (TimedVm) vm;
		
		// Used only to check the creation time.
		System.out.println(CloudSim.clock() + " Creating VM " + vm.getUid() + " , Id " + vm.getId());
		
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, tvm.getCandidateHost().getHost());

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			}
			else {
				data[2] = CloudSimTags.FALSE;
			}
			
			send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, data);
		}

		if (result) {
			getVmList().add(vm);
			tvm.setActive(true);

			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}

			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
		}
		
		if(ack) {
			send(nos.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, ev.getData());
		}
			
	}
	
	@Override
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		
		TimedVm tvm = (TimedVm) vm;
		
		// Used only to check the destruction time.
		System.out.println(CloudSim.clock() + " Destroying VM " + vm.getUid() + " , Id " + vm.getId());
		
		getVmAllocationPolicy().deallocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;

			sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, data);
		}

		getVmList().remove(vm);
		
		tvm.setActive(false);
	}
	
	@Override
	public void processOtherEvent(SimEvent ev){
		
		switch(ev.getTag()){
			case Constants.REQUEST_SUBMIT:
				processRequest((Request) ev.getData());
				break;
			case Constants.APPLICATION_SUBMIT:
				processApplication(ev.getSource(), (String) ev.getData());
				break;
			case CloudSimTags.VSWITCH_CREATE_ACK:
				processVSwitchCreate(ev, true);
				break;
			case CloudSimTags.VSWITCH_DESTROY:
				processVSwitchDestroy(ev, false);
				break;
			case Constants.DEPLOY_APPLICATION:
				String []data = (String [])ev.getData();
				deployApplication(Integer.parseInt(data[0]), data[1]);
				break;
			default: 
				System.out.println("Unknown event received by SdnDatacenter. Tag:" + ev.getTag());
		}
	}

	@Override
	protected void checkCloudletCompletion() {
		if(!nos.isApplicationDeployed()) {
			super.checkCloudletCompletion();
			return;
		}
		
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		
		for (int i = 0 ; i < list.size() ; i++) {
			Host host = list.get(i);
			
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					
					if (cl != null) {
						int hostAddress = nos.getHostAddressByVmId(cl.getVmId());
						sendNow(hostAddress, CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
	}
		
	private void processRequest(Request req) {
		// Request received from user. Send to SdnHost.
		Activity ac = req.getNextActivity();
		
		System.out.println("Received REQUEST_SUBMIT from Broker at " + this.getName());
		
		if(ac instanceof Processing) {
			Cloudlet cl = ((Processing) ac).getCloudlet();
			int hostAddress = nos.getHostAddressByVmId(cl.getVmId());
			
			// For this first package, size doesn't matter.
			Package pkg = new Package(super.getId(), cl.getVmId(), -1, -1, req);
			sendNow(hostAddress, Constants.SDN_PACKAGE, pkg);
		}
		else {
			System.err.println("Request should start with Processing!!");
		}
	}
	
	/**
	 * Processes application request and schedules the deployment at the start time of the datacenter.
	 * @param userId
	 * @param filename
	 */
	private void processApplication(int userId, String filename) {
		
		nos.readVirtualNetwork(userId, filename);
		
		String []sendData = {Integer.toString(userId), filename};
		
		send(this.getId(), this.getStartTime() + CloudSim.getMinTimeBetweenEvents(), 
				Constants.DEPLOY_APPLICATION, sendData);
	}
	
	/**
	 * Deploys the virtual datacenter and its workload if it succeeds.
	 * @param userId
	 * @param filename
	 */
	private void deployApplication(int userId, String filename){
		boolean result = nos.deployApplication(userId);
		
		if (result) {
			// Deploying workload.
			send(userId, CloudSim.getMinTimeBetweenEvents(), Constants.APPLICATION_SUBMIT_ACK, filename);
		}
		else {
			System.out.println("Could not deploy Virtual Datacenter");
		}
	}
	
	public Map<String, Integer> getVmNameIdTable() {
		return this.nos.getVmNameIdTable();
	}
	
	public Map<String, Integer> getFlowNameIdTable() {
		return this.nos.getFlowNameIdTable();
	}
	
	public List<VSwitch> getVSwitchList() {
		return this.vswitchList;
	}
	
	public void processVSwitchCreate(SimEvent ev, boolean ack) {
		VSwitch vswitch = (VSwitch) ev.getData();
		Switch pswitch = vswitch.getSwitch();
		if (pswitch.vswitchCreate(vswitch)) {
			getVSwitchList().add(vswitch);
			vswitch.setActive(true);
		}
		if (ack) {
			send(nos.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VSWITCH_CREATE_ACK, ev.getData());
		}
	}
	
	public void processVSwitchDestroy(SimEvent ev, boolean ack) {
		VSwitch vswitch = (VSwitch) ev.getData();
		Switch pswitch = vswitch.getSwitch();
		
		System.out.println(CloudSim.clock() + " Destroying VSwitch Id " + vswitch.getId());
		
		if (pswitch.vswitchDestroy(vswitch)) {
			getVSwitchList().remove(vswitch);
			vswitch.setActive(false);
		}
		
		if (ack) {
			/*
			 * TODO: Do we need to send an ack to Broker, this todo applies even to VSWITCH_CREATE_ACK. 
			 */ 
		}
	}
}
