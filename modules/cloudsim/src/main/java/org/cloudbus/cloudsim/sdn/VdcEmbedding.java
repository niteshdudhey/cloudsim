/**
 * 
 */
package org.cloudbus.cloudsim.sdn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Vm;

/**
 * The class to represent a VDC embedding on a Physical Datacenter.
 * An object of this will be returned when the VDCEPolicy will be asked to find the embedding for the VDC.
 *  
 * @author Nitesh Dudhey
 *
 */
public class VdcEmbedding {
	
	// Prefer to have an explicit Vm to Host mapping rather than just id - id mapping
	Map<Vm, SDNHost> vmMap;
	
	// VSwitch to Switch mapping
	Map<VSwitch, Switch> vswitchMap;
	
	// Arc (VLink) to Link mapping
	Map<Arc, List<Link>> vlinkMap;
	
	public VdcEmbedding() {
		vmMap = new HashMap<Vm, SDNHost>();
		vswitchMap = new HashMap<VSwitch, Switch>();
		vlinkMap = new HashMap<Arc, List<Link>>();
	}
	
	public VdcEmbedding(Map<Vm, SDNHost> vmMap, Map<VSwitch, Switch> vswitchMap, Map<Arc, List<Link>> vlinkMap) {
		this.vmMap = vmMap;
		this.vswitchMap = vswitchMap;
		this.vlinkMap = vlinkMap;
	}
	
	public Map<Vm, SDNHost> getVmMap() {
		return vmMap;
	}
	
	public void setVmMap(Map<Vm, SDNHost> vmMap) {
		this.vmMap = vmMap;
	}
	
	public Map<VSwitch, Switch> getVSwitchMap() {
		return vswitchMap;
	}
	
	public void setVSwitchMap(Map<VSwitch, Switch> vswitchMap) {
		this.vswitchMap = vswitchMap;
	}
	
	public Map<Arc, List<Link>> getVLinkMap() {
		return vlinkMap;
	}
	
	public void setVLinkMap(Map<Arc, List<Link>> vlinkMap) {
		this.vlinkMap = vlinkMap;
	}
	
	public SDNHost getAllocatedHostForVm(Vm vm){
		return vmMap.get(vm);
	}
	
	public void allocateVmToHost(Vm vm, SDNHost host){
		vmMap.put(vm, host);
	}
	
	public Switch getAllocatedSwitchForVSwitch(VSwitch vswitch) {
		return vswitchMap.get(vswitch);
	}
	
	public void allocateVSwitchToSwitch(VSwitch vswitch, Switch pswitch){
		vswitchMap.put(vswitch, pswitch);
	}
	
	public List<Link> getAllocatedLinksForVLink(Arc arc){
		return vlinkMap.get(arc);
	}
	
	public void allocateVLinkToLinks(Arc arc, List<Link> links){
		vlinkMap.put(arc, links);
	}
	
	public String toString(){
		StringBuilder ret = new StringBuilder();
		
		ret.append("\nVm to Host Mapping\n");
		ret.append(vmMap.toString());
		
		ret.append("\nVSwitch to Switch Mapping\n");
		ret.append(vswitchMap.toString());
		
		ret.append("\nVLink to Links Mapping\n");
		ret.append(vlinkMap.toString());
		
		return ret.toString();
	}
}
