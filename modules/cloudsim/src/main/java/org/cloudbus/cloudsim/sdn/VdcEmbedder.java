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
	
	/**
	 * The method initializes the embedder.
	 */
	public void init(PhysicalTopology topology);
	
	
	/**
	 * The method returns a VDCEmbedding of a VDC to a physical network.
	 *  
	 * @param physicalTopology
	 * @param virtualTopology
	 * @return
	 */
	public VdcEmbedding embed(PhysicalTopology physicalTopology, VirtualTopology virtualTopology);
	
	public void deallocateVm(PhysicalTopology topology, TimedVm tvm);
	
}
