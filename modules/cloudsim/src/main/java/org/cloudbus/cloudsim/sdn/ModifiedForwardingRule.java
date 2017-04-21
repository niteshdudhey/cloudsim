/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

import java.util.Map;

import org.cloudbus.cloudsim.Log;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


/**
 * ForwardingRule class is to represent a forwarding table in each switch.
 * This is for VM routing, not host routing. Addresses used here are the addresses of VM.
 *  
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */
public class ModifiedForwardingRule {
	
	Table<Integer, Node, Node> table;

	public ModifiedForwardingRule(){
		this.table = HashBasedTable.create();
	}
	
	public void clear(){
		table.clear();
	}
	
	public void addRule(int flowId, Node from, Node to){
		table.put(flowId, from, to);
	}
	
	public void removeRule(int flowId, Node from){
		table.remove(flowId, from);
	}

	public Node getRoute(int flowId, Node from) {
		return table.get(flowId, from);
	}
	
	public void printForwardingTable(String thisNode) {
		
		for(Integer rowK : table.rowKeySet()) {
			
			Map<Node, Node> row = table.row(rowK);
			
			for(Node colK : row.keySet()) {
				Node node = row.get(colK);
				if(node instanceof SDNHost) {
					Log.printLine(thisNode + ": " +
							NetworkOperatingSystem.debugVmIdName.get(rowK) + "->" +  
							NetworkOperatingSystem.debugVmIdName.get(colK) + "->" + "(flow:" + rowK + ")" +   
							((SDNHost) node).getName());
				}
				
				else if(node instanceof Switch) {
					Log.printLine(thisNode + ": " + 
							NetworkOperatingSystem.debugVmIdName.get(rowK) + "->" + 
							NetworkOperatingSystem.debugVmIdName.get(colK) + "->" + "(flow:" + rowK + ")" + 
							((Switch) node).getName());
				}
				
				else {
					Log.printLine(thisNode + ": " +
							NetworkOperatingSystem.debugVmIdName.get(rowK) + "->" + 
							NetworkOperatingSystem.debugVmIdName.get(colK) + "->" + "(flow:" + rowK + ")" + 
							node.getAddress());
				}
			}
		}
	}
}
