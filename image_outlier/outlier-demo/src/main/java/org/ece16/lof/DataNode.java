package org.ece16.lof;

import java.util.ArrayList;
import java.util.List;  

/** 
 *  
 * 
 * original source: https://github.com/wilsonact/LOF-java/blob/master/DataNode.java
 * 
 */  
public class DataNode {  
	private String nodeName;
	private String nodeLabel;
	private double[] dimension;

	private double kDistance; // k-??  
	private List<DataNode> kNeighbor = new ArrayList<DataNode>(); // k-??  
	private double distance; // ???????????  
	private double reachDensity;// ????  
	private double reachDis;// ????  

	private double lof;// ??????  

	public DataNode() {
	}  

	public DataNode(DataNode node) {
		this(node.nodeName, node.nodeLabel, node.dimension);
	}

	public DataNode(String nodeName, String nodeLabel, double[] dimension) {  
		this.nodeName = nodeName;  
		this.nodeLabel = nodeLabel;  
		this.dimension = dimension;  
	}  

	/**
	 * @param node
	 * @return Euclidian distance between the two nodes
	 */
	public double distance(DataNode node) {  
		double dis = 0.0;  

		for (int i = 0; i < dimension.length; i++) {  
			dis += Math.pow(dimension[i] - node.dimension[i], 2);  
		}

		return Math.pow(dis, 0.5);  
	}  

	public String getNodeName() {  
		return nodeName;  
	}  

	public void setNodeName(String nodeName) {  
		this.nodeName = nodeName;  
	}  

	public String getNodeLabel() {  
		return nodeLabel;  
	}  

	public void setNodeLabel(String nodeLabel) {  
		this.nodeLabel = nodeLabel;  
	}  

	public double[] getDimensioin() {  
		return dimension;  
	}  

	public void setDimensioin(double[] dimensioin) {  
		this.dimension = dimensioin;  
	}

	public double getkDistance() {  
		return kDistance;  
	}  

	public void setkDistance(double kDistance) {  
		this.kDistance = kDistance;  
	}  

	public List<DataNode> getkNeighbor() {  
		return kNeighbor;  
	}  

	public void setkNeighbor(List<DataNode> kNeighbor) {  
		this.kNeighbor = kNeighbor;  
	}  

	public double getDistance() {  
		return distance;  
	}  

	public void setDistance(double distance) {  
		this.distance = distance;  
	}  

	public double getReachDensity() {  
		return reachDensity;  
	}  

	public void setReachDensity(double reachDensity) {  
		this.reachDensity = reachDensity;  
	}  

	public double getReachDis() {  
		return reachDis;  
	}  

	public void setReachDis(double reachDis) {  
		this.reachDis = reachDis;  
	}  

	public double getLof() {  
		return lof;  
	}  

	public void setLof(double lof) {  
		this.lof = lof;  
	}  
}  