/*
 * Title:        CloudSimSDN
 * Description:  SDN extension for CloudSim
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2015, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.sdn;

/**
 * Constant variables to use
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
	public static final int APPLICATION_SUBMIT = SDN_BASE + 20;	// Broker -> Datacenter.
	public static final int APPLICATION_SUBMIT_ACK = SDN_BASE + 21;
	
	public static String TagText(int tagValue) {
		tagValue = tagValue - SDN_BASE;
        switch (tagValue) {
        case 1:
            return "SDN_PACKAGE";
        case 2:
            return "SDN_INTERNAL_PACKAGE_PROCESS";
        case 10:
            return "REQUEST_SUBMIT";
        case 11:
            return "REQUEST_COMPLETED";
        case 20:
            return "APPLICATION_SUBMIT";
        case 21:
            return "APPLICATION_SUBMIT_ACK";
        default:
            return "Invalid tag value";
        }
    }
	
}
