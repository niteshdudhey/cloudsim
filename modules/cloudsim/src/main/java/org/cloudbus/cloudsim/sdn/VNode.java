package org.cloudbus.cloudsim.sdn;

import java.util.List;

public interface VNode {
	
	public List<VNode> getUpperVNodes();
	
	public List<VNode> getLowerVNodes();
	
	public void addUpperVNode(VNode upperNode);
	
	public void addLowerVNode(VNode lowerNode);
	
}
