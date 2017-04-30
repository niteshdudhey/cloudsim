/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cloudbus.cloudsim.Log;

/**
 * This is physical link between hosts and switches to build physical topology.
 * Links have latency and bandwidth.
 *  
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */
public class Link {
	// Bi-directional link (one link = both ways).
	
	Node highOrder;
	Node lowOrder;
	double upBW;	// low -> high
	double downBW;	// high -> low
	double latency;
	
	// Data Members required for VLink embedding
	double availableBw;
	List<Arc> vlinksList;
	
	private List<Channel> upChannels;
	private List<Channel> downChannels;
	
	private String name;
	
	Set<Double> timeForRecordedHistory  = new HashSet<Double>();
	
	public List<LinkStateHistoryEntry> stateHistory = new LinkedList<LinkStateHistoryEntry>();
	
	// Added for checking BW Usage
	public void storeCurrentState(double time) {
		stateHistory.add(new LinkStateHistoryEntry(time, Math.min(getFreeBandwidth(highOrder), getFreeBandwidth(lowOrder))));
	}
	
	public Link(Node highOrder, Node lowOrder, double latency, double bw) {
		this.highOrder = highOrder;
		this.lowOrder = lowOrder;
		this.upBW = this.downBW = bw;
		this.latency = latency;
		
		this.availableBw = bw; // Initially all Bw is available
		
		this.upChannels = new ArrayList<Channel>();
		this.downChannels = new ArrayList<Channel>();
		
		this.vlinksList = new ArrayList<Arc>();
		
		this.name = highOrder.getName() + "-" + lowOrder.getName();
	}
	
	public Link(Node highOrder, Node lowOrder, double latency, double upBW, double downBW) {
		this(highOrder, lowOrder, latency, upBW);
		this.downBW = downBW;
	}

	public void updateUtilizationHistory(double time){
		if(!timeForRecordedHistory.contains(time)){
			stateHistory.add(new LinkStateHistoryEntry(time, Math.min(getFreeBandwidth(highOrder), getFreeBandwidth(lowOrder))));
			timeForRecordedHistory.add(time);
		}
	}
	
	public Node getHighOrder() {
		return highOrder;
	}

	public Node getLowOrder() {
		return lowOrder;
	}
	
	public double getAvailableBw() {
		return availableBw;
	}
	
	public void setAvailableBw(double bw) { 
		this.availableBw = bw;
	}
	
	public List<Arc> getVLinksList() {
		return vlinksList;
	}
	
	// Also subtracts the bw requested by arc/vlink
	public boolean addVLink(Arc arc) {
		if (arc.getBw() > this.availableBw) {
			System.err.println("Cannot add Arc with required Bw higher than available.");
			return false;
		} else {
			setAvailableBw(this.availableBw-arc.getBw());
			vlinksList.add(arc);
			return true;
		}
	}
	
	// Also adds the bw allocated to arc/vlink
	public boolean removeVLink(Arc arc) {
		if (!vlinksList.contains(arc)) {
			System.err.println("Cannot remove unembedded VLink.");
			return false;
		} else {
			setAvailableBw(this.availableBw+arc.getBw());
			vlinksList.remove(arc);
			return true;
		}
	}
	
	public Node getOtherNode(Node from) {
		if(highOrder.equals(from)) {
			return lowOrder;
		}
		
		return highOrder;
	}
	
	private boolean isUplink(Node from) {
		if(from.equals(lowOrder)) {
			return true;
		}
		
		else if(from.equals(highOrder)) {
			return false;
		}
		
		else {
			throw new IllegalArgumentException("Link.isUplink(): from(" + from + ") Node is wrong!!");			
		}
	}
	
	public String getName(){
		return name;
	}
	
	public double getBw(Node from) {
		if(isUplink(from)) {
			return upBW;
		}
		
		else {
			return downBW;
		}
	}
	
	public double getBw() {
		if(upBW != downBW) {
			throw new IllegalArgumentException("Downlink/Uplink BW are different!");
		}
		
		return upBW;
	}

	public double getLatency() {
		return latency;
	}
	
	private List<Channel> getChannels(Node from) {
		List<Channel> channels;
		
		if(isUplink(from)) {
			channels = this.upChannels;
		}
		
		else {
			channels = this.downChannels;
		}

		return channels;
	}
	
	public double getDedicatedChannelAdjustFactor(Node from) {
		double factor = 1.0;
		double totalRequested = getRequestedBandwidthForDedicatedChannels(from);
		
		if(totalRequested > this.getBw()) {
			Log.printLine("Link.getDedicatedChannelAdjustFactor() Exceeds link bandwidth. Reduce requested bandwidth");
			
			factor = this.getBw() / totalRequested;
		}
		
		return factor;
	}
	
	public boolean addChannel(Node from, Channel ch) {
		getChannels(from).add(ch);
		
		return true;
	}
	
	public boolean removeChannel(Channel ch) {
		boolean ret = this.upChannels.remove(ch);
		
		if(!ret) {
			// The channel is down link.
			ret = this.downChannels.remove(ch);
		}
		
		return ret;
	}
	
	public double getAllocatedBandwidthForDedicatedChannels(Node from) {
		
		double bw = 0;
		
		for(Channel ch : getChannels(from)) {
			if(ch.getChId() != -1) {
				// chId == -1 => default channel.
				bw += ch.getAllocatedBandwidth();
			}
		}
		
		return bw;
	}

	public double getRequestedBandwidthForDedicatedChannels(Node from) {
		
		double bw = 0;
		
		for(Channel ch : getChannels(from)) {
			if(ch.getChId() != -1) {
				// chId == -1 => default channel.
				bw += ch.getRequestedBandwidth();
			}
		}
		
		return bw;
	}

	public int getChannelCount(Node from) {
		List<Channel> channels =  getChannels(from);
		
		return channels.size();
	}
	
	public int getDedicatedChannelCount(Node from) {
		int num = 0;
		
		for(Channel ch : getChannels(from)) {
			if(ch.getChId() != -1) {
				// chId == -1 => default channel.
				num ++;
			}
		}
		
		return num;
	}
	
	public int getSharedChannelCount(Node from) {
		int num =  getChannels(from).size() - getDedicatedChannelCount(from);
		
		return num;
	}
	
	public double getFreeBandwidth(Node from) {
		double bw = this.getBw(from);
		double dedicatedBw = getAllocatedBandwidthForDedicatedChannels(from);
		
		return bw - dedicatedBw;
	}

	public double getFreeBandwidthForDedicatedChannel(Node from) {
		double bw = this.getBw(from);
		double dedicatedBw = getRequestedBandwidthForDedicatedChannels(from);
		
		return bw - dedicatedBw;
	}

	public double getSharedBandwidthPerChannel(Node from, Node to) {
		double freeBw = getFreeBandwidth(from);
		double sharedBwEachChannel = freeBw / getSharedChannelCount(from);
		
		return sharedBwEachChannel;
	}

	public String toString() {
		StringBuilder ret = new StringBuilder();
		
		ret.append("Link:");
		ret.append(this.highOrder.toString());
		ret.append(" <-> ");
		ret.append(this.lowOrder.toString());
		
		ret.append(", upBW:");
		ret.append(upBW);
		
		ret.append(", Latency:");
		ret.append(latency);
		
		return ret.toString();
	}
	
	public boolean isActive() {
		if(this.upChannels.size() > 0 || this.downChannels.size() > 0) {
			return true;
		}

		return false;
	}

	
	public boolean isInternal() {
		return highOrder==lowOrder;
	}
}

