/**
 * 
 */
package org.cloudbus.cloudsim.sdn.datacenterSpecifications;

import java.util.List;

/**
 * The class that represent all the datacenters.
 * Assumption: all Hosts have same specifications. (Similarly VMs and Switches)
 * @author Nitesh Dudhey
 *
 */

public class AllDatacentersSpec {
	// PDC Spec.
	PDCSpecShort pdc;
	
	// VDC spec
	List<VDCSpecShort> vdcs;

	public PDCSpecShort getPdc() {
		return pdc;
	}

	public void setPdc(PDCSpecShort pdc) {
		this.pdc = pdc;
	}

	public List<VDCSpecShort> getVdcs() {
		return vdcs;
	}

	public void setVdcs(List<VDCSpecShort> vdcs) {
		this.vdcs = vdcs;
	}
	
}