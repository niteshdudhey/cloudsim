/**
 * 
 */
package org.cloudbus.cloudsim.sdn;

/**
 * The interface whose implementation will be provided by the user.
 * @author Nitesh Dudhey
 *
 */
public interface VdcEmbedder {
	// The method returns a VDCEmbedding of a VDC to a physical network.
	public VdcEmbedding embed(PhysicalTopology physicalTopology, VirtualTopology virtualTopology);
}
