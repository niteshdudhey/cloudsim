package org.cloudbus.cloudsim.sdn;

public class VDCRequestMetrics {
	
	double time;
	
	int totalRequestsCount;
	
	int waitingRequestsCount;
	
	int deployedRequestsCount;
	
	public VDCRequestMetrics(double time, int totalRequestsCount, int waitingRequestsCount, 
			int deployedRequestsCount) {
		this.time = time;
		this.totalRequestsCount = totalRequestsCount;
		this.waitingRequestsCount = waitingRequestsCount;
		this.deployedRequestsCount = deployedRequestsCount;
	}
	
	public String toString() {
		String str = "";
		str += "Total Number of VDC Requests = " + totalRequestsCount;
		str += "\n";
		str += "Number of pending VDC Requests = " + waitingRequestsCount;
		str += "\n";
		str += "Number of served VDC Requests = " + deployedRequestsCount;
		str += "\n";
		return str;
	}
	
	public double getTime() {
		return time;
	}
	
	public void setTime(double time) {
		this.time = time;
	}
	
	public int getTotalRequestsCount() {
		return totalRequestsCount;
	}
	
	public void setTotalRequestsCount(int requestsCount) {
		this.totalRequestsCount = requestsCount;
	}

	public int getWaitingRequestsCount() {
		return waitingRequestsCount;
	}
	
	public void setWaitingRequestsCount(int requestsCount) {
		this.waitingRequestsCount = requestsCount;
	}
	
	public int getDeployedRequestsCount() {
		return deployedRequestsCount;
	}
	
	public void setDeployedRequestsCount(int requestsCount) {
		this.deployedRequestsCount = requestsCount;
	}

}
