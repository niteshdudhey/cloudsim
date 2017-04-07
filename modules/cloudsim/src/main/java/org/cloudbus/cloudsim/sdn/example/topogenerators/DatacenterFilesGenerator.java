/**
 * 
 */
package org.cloudbus.cloudsim.sdn.example.topogenerators;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.cloudbus.cloudsim.sdn.datacenterSpecifications.AllDatacentersSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.HostSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.LinkSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.PDCSpecShort;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.PdcSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.SwitchSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VDCSpecShort;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VLinkSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VSwitchSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VdcSpec;
import org.cloudbus.cloudsim.sdn.datacenterSpecifications.VmSpec;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * The class to generate physical and virtual topology files.
 * The specification is to be given from another JSON file.
 * 
 * @author Nitesh Dudhey
 *
 */
public class DatacenterFilesGenerator {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 2) {
			System.exit(1);
		}
		
		String datacentersSpecFile = "";
		String folderLocation = "";
		
		datacentersSpecFile = args[0];
		folderLocation = args[1];
		
		Gson gson = new Gson();
		
		AllDatacentersSpec allDCs = null;
		
		try {
			allDCs = gson.fromJson(new FileReader(datacentersSpecFile), AllDatacentersSpec.class);
		} 
		catch (JsonSyntaxException e1) {
			e1.printStackTrace();
		} 
		catch (JsonIOException e1) {
			e1.printStackTrace();
		} 
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try (FileWriter writer = new FileWriter(folderLocation + "\\PDC.json")) {
			PdcSpec pdc = generatePdc(allDCs.getPdc());
			gson.toJson(pdc, writer);
			System.out.println(folderLocation + "\\PDC.json");
		} 
		catch (IOException e) {
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int i = 0;
		for(VDCSpecShort vdcShort : allDCs.getVdcs()){
			try (FileWriter writer = new FileWriter(folderLocation + "\\VDC" + i + ".json")) {
				VdcSpec vdc = generateVdc(vdcShort);
				gson.toJson(vdc, writer);
				System.out.println(folderLocation + "\\VDC" + i + ".json");
			}
			catch (CloneNotSupportedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			i++;
		}
	}

	private static VdcSpec generateVdc(VDCSpecShort vdcShort) throws CloneNotSupportedException {
		
		VdcSpec vdc = new VdcSpec();
		
		vdc.setStarttime(vdcShort.getStarttime());
		
		vdc.setEndtime(vdcShort.getEndtime());
		
		VSwitchSpec coreSwitch = (VSwitchSpec)vdcShort.getCoreSwitchSpec().clone();
		
		vdc.addSwitch(coreSwitch);
		
		for(int i = 0 ; i < vdcShort.getCoreSwitchFanout() ; i++){
			VSwitchSpec aggregateSwitch = (VSwitchSpec)vdcShort.getAggregateSwitchSpec().clone();
			aggregateSwitch.setName(aggregateSwitch.getName() + "_" + i);
			
			VLinkSpec link1 = (VLinkSpec)vdcShort.getLinkSpec().clone();
			link1.setSource(coreSwitch.getName());
			link1.setDestination(aggregateSwitch.getName());
			link1.setName(link1.getName() + "-" + coreSwitch.getName() + "-" + aggregateSwitch.getName());
			
			vdc.addSwitch(aggregateSwitch);
			vdc.addLink(link1);
			
			for(int j = 0 ; j < vdcShort.getAggregateSwitchFanout() ; j++) {
				VSwitchSpec edgeSwitch = (VSwitchSpec)vdcShort.getEdgeSwitchSpec().clone();
				edgeSwitch.setName(edgeSwitch.getName() + "_" + i + "_" + j);
				
				VLinkSpec link2 = (VLinkSpec)vdcShort.getLinkSpec().clone();
				link2.setSource(aggregateSwitch.getName());
				link2.setDestination(edgeSwitch.getName());
				link2.setName(link2.getName() + "-" + aggregateSwitch.getName() + "-" + edgeSwitch.getName());
				
				vdc.addSwitch(edgeSwitch);
				vdc.addLink(link2);
				
				for(int k = 0 ; k < vdcShort.getEdgeSwitchFanout() ; k++) {
					VmSpec vm = (VmSpec)vdcShort.getVmSpec().clone();
					vm.setName(vm.getName() + "_" + i + "_" + j + "_" + k);
					
					VLinkSpec link3 = (VLinkSpec)vdcShort.getLinkSpec().clone();
					link3.setSource(edgeSwitch.getName());
					link3.setDestination(vm.getName());
					link3.setName(link3.getName() + "-" + edgeSwitch.getName() + "-" + vm.getName());
					
					vdc.addVm(vm);
					vdc.addLink(link3);
				}
			}
		}
		
		return vdc;
	}

	private static PdcSpec generatePdc(PDCSpecShort pdcShort) throws CloneNotSupportedException {
		
		PdcSpec pdc = new PdcSpec();
		
		SwitchSpec coreSwitch = (SwitchSpec)pdcShort.getCoreSwitchSpec().clone();
		pdc.addSwitch(coreSwitch);
		
		for(int i = 0 ; i < pdcShort.getCoreSwitchFanout() ; i++){
			SwitchSpec aggregateSwitch = (SwitchSpec)pdcShort.getAggregateSwitchSpec().clone();
			aggregateSwitch.setName(aggregateSwitch.getName() + "_" + i);
			
			LinkSpec link1 = (LinkSpec)pdcShort.getLinkSpec().clone();
			link1.setSource(coreSwitch.getName());
			link1.setDestination(aggregateSwitch.getName());
			
			pdc.addSwitch(aggregateSwitch);
			pdc.addLink(link1);
			
			for(int j = 0 ; j < pdcShort.getAggregateSwitchFanout() ; j++) {
				SwitchSpec edgeSwitch = (SwitchSpec)pdcShort.getEdgeSwitchSpec().clone();
				edgeSwitch.setName(edgeSwitch.getName() + "_" + i + "_" + j);
				
				LinkSpec link2 = (LinkSpec)pdcShort.getLinkSpec().clone();
				link2.setSource(aggregateSwitch.getName());
				link2.setDestination(edgeSwitch.getName());
				
				pdc.addSwitch(edgeSwitch);
				pdc.addLink(link2);
				
				for(int k = 0 ; k < pdcShort.getEdgeSwitchFanout() ; k++) {
					HostSpec host = (HostSpec)pdcShort.getHostSpec().clone();
					host.setName(host.getName() + "_" + i + "_" + j + "_" + k);
					
					LinkSpec link3 = (LinkSpec)pdcShort.getLinkSpec().clone();
					link3.setSource(edgeSwitch.getName());
					link3.setDestination(host.getName());
					
					pdc.addHost(host);
					pdc.addLink(link3);
				}
			}
		}
		
		return pdc;
	}
}
