/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

/**
 * Constant variables to use.
 * 
 * @author Jungmin Son
 * @author Rodrigo N. Calheiros
 * @since CloudSimSDN 1.0
 */

public class Constants {
	
	private static final int SDN_BASE = 89000000;
	
	public static final int SDN_PACKAGE = SDN_BASE + 1;
	
	public static final int SDN_INTERNAL_PACKAGE_PROCESS = SDN_BASE + 2; 
	
	public static final int REQUEST_SUBMIT = SDN_BASE + 10;
	
	public static final int REQUEST_COMPLETED = SDN_BASE + 11;
	
	public static final int APPLICATION_SUBMIT = SDN_BASE + 20;	 // Broker -> Datacenter.
	
	public static final int APPLICATION_SUBMIT_ACK = SDN_BASE + 21;
	
	// Deploy application at SDNDatacenter.
	public static final int DEPLOY_APPLICATION = SDN_BASE + 22;
	
	public static String TagText(int tagValue) {
		tagValue = tagValue - SDN_BASE;
        String ret = "";
        		
		switch (tagValue) {
        	case 1:
        		ret = "SDN_PACKAGE";
        		break;
        	case 2:
        		ret = "SDN_INTERNAL_PACKAGE_PROCESS";
        		break;
        	case 10:
        		ret = "REQUEST_SUBMIT";
        		break;
        	case 11:
        		ret = "REQUEST_COMPLETED";
        		break;
        	case 20:
        		ret = "APPLICATION_SUBMIT";
        		break;
        	case 21:
        		ret = "APPLICATION_SUBMIT_ACK";
        		break;
        	default:
        		ret = "Invalid tag value";
        		break;
        }
        
        return ret;
    }
}
