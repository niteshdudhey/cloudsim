/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

import java.util.List;

/**
 * Node represents network node (host or switch)
 *  
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */
public interface Node {
	
	int getAddress();
	
	public String getName();
	
	public long getBandwidth();
	
	public void setRank(int rank);
	
	public int getRank();
	
	public void clearVMRoutingTable();
	
	public void addVMRoute(int srcVM, int destVM, int flowId, Node to);
	
	public Node getVMRoute(int srcVM, int destVM, int flowId);
	
	public void removeVMRoute(int srcVM, int destVM, int flowId);
	
	public void printVMRoute();

	public void addRoute(Node destHost, Link to);
	
	public List<Link> getRoute(Node destHost);
	
	public RoutingTable getRoutingTable();

	public void addLink(Link l);
	
	public void updateNetworkUtilization();
	
	public void storeCurrentState(double time);
	
	public List<Node> getUpperNodes();
	
	public List<Node> getLowerNodes();
	
	public void addUpperNode(Node upperNode);
	
	public void addLowerNode(Node lowerNode);
	
	public void clearModifiedVMRoutingTable();

	public void addModifiedVMRoute(int flowId, Node from, Node to);

	public Node getModifiedVMRoute(int flowId, Node from);

	public void removeModifiedVMRoute(int flowId, Node from);

	public void printModifiedVMRoute();
	
}
