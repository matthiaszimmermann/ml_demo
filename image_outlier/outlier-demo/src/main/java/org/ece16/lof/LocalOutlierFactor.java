package org.ece16.lof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;  

/** 
 * original source: https://github.com/wilsonact/LOF-java/blob/master/DataNode.java
 *
 * Implementation of the LOF algorithm for local density based outlier detection.
 * paper: http://www.dbs.ifi.lmu.de/Publikationen/Papers/LOF.pdf
 * wiki: https://en.wikipedia.org/wiki/Local_outlier_factor 
 */  
public class LocalOutlierFactor {  

	private int INT_K = 4;

	public void setK(int int_k) {  
		this.INT_K = int_k;  
	}

	/** 
	 * Comparator for sorting DataNode using the distance in ascending order.
	 */  
	public class DistComparator implements Comparator<DataNode> {  
		public int compare(DataNode A, DataNode B) {  
			if((A.getDistance() - B.getDistance()) < 0)     
				return -1;    
			else if((A.getDistance() - B.getDistance()) > 0)    
				return 1;    
			else return 0;    
		}  
	}  

	/** 
	 * Comparator for sorting DataNode using the LOF value in descending order.
	 */  
	public class LofComparator implements Comparator<DataNode> {  
		public int compare(DataNode A, DataNode B) {  
			if((A.getLof() - B.getLof()) < 0)     
				return 1;    
			else if((A.getLof() - B.getLof()) > 0)    
				return -1;    
			else return 0;    
		}  
	}    

	/**
	 * Computes LOF values for all nodes and returns a sorted set of the nodes.
	 */
	public List<DataNode> getOutlierNodes(List<DataNode> allNodes) {  

		List<DataNode> kdAndKnList = getKDAndKN(allNodes);  
		calReachDis(kdAndKnList);  
		calReachDensity(kdAndKnList);  
		calLof(kdAndKnList);  
		Collections.sort(kdAndKnList, new LofComparator());  

		return kdAndKnList;  
	}  

	/** 
	 * @param kdAndKnList 
	 */  
	private void calLof(List<DataNode> kdAndKnList) {  
		for (DataNode node : kdAndKnList) {  
			List<DataNode> tempNodes = node.getkNeighbor();  
			double sum = 0.0;  
			for (DataNode tempNode : tempNodes) {  
				double rd = getRD(tempNode.getNodeName(), kdAndKnList);  
				sum = rd / node.getReachDensity() + sum;  
			}  
			sum = sum / (double) INT_K;  
			node.setLof(sum);
		}  
	}  

	/** 
	 * @param kdAndKnList 
	 */  
	private void calReachDensity(List<DataNode> kdAndKnList) {  
		for (DataNode node : kdAndKnList) {  
			List<DataNode> tempNodes = node.getkNeighbor();  
			double sum = 0.0;  
			double rd = 0.0;  
			for (DataNode tempNode : tempNodes) {  
				sum = tempNode.getReachDis() + sum;  
			}
			rd = (double) INT_K / sum;
			node.setReachDensity(rd); 
		}
	}

	/** 
	 * reachdis(p,o)=max{ k-distance(o), d(p,o)} 
	 * @param kdAndKnList 
	 */  
	private void calReachDis(List<DataNode> kdAndKnList) {  
		for (DataNode node : kdAndKnList) {  
			List<DataNode> tempNodes = node.getkNeighbor();  
			for (DataNode tempNode : tempNodes) {  
				double kDis = getKDis(tempNode.getNodeName(), kdAndKnList);  
				//reachdis(p,o)=max{ k-distance(o),d(p,o)}  
				if (kDis < tempNode.getDistance()) {  
					tempNode.setReachDis(tempNode.getDistance());  
				} else {  
					tempNode.setReachDis(kDis);  
				}  
			}  
		}  
	}  

	/** 
	 * Returns the kDistance of the specified node. 
	 * @param nodeName 
	 * @param nodeList 
	 * @return 
	 */  
	private double getKDis(String nodeName, List<DataNode> nodeList) {  
		double kDis = 0;  
		for (DataNode node : nodeList) {  
			if (nodeName.trim().equals(node.getNodeName().trim())) {  
				kDis = node.getkDistance();  
				break;  
			}  
		}  
		return kDis;  

	}  

	/** 
	 * Returns reach density of specified node.
	 * @param nodeName 
	 * @param nodeList 
	 * @return 
	 */  
	private double getRD(String nodeName, List<DataNode> nodeList) {  
		double kDis = 0;  
		for (DataNode node : nodeList) {  
			if (nodeName.trim().equals(node.getNodeName().trim())) {  
				kDis = node.getReachDensity();  
				break;  
			}  
		}  
		return kDis;  

	}  

	/** 
	 * @param allNodes 
	 * @return List<Node> 
	 */  
	private List<DataNode> getKDAndKN(List<DataNode> allNodes) {  
		List<DataNode> kdAndKnList = new ArrayList<DataNode>();  
		for (int i = 0; i < allNodes.size(); i++) {  
			List<DataNode> tempNodeList = new ArrayList<DataNode>();  
			DataNode nodeA = new DataNode(allNodes.get(i));  

			for (int j = 0; j < allNodes.size(); j++) {  
				DataNode nodeB = new DataNode(allNodes.get(j));  
				double tempDis = nodeA.distance(nodeB);
				nodeB.setDistance(tempDis);  
				tempNodeList.add(nodeB);  
			}  

			Collections.sort(tempNodeList, new DistComparator());  
			for (int k = 1; k < INT_K; k++) {  
				nodeA.getkNeighbor().add(tempNodeList.get(k));  
				if (k == INT_K - 1) {  
					nodeA.setkDistance(tempNodeList.get(k).getDistance());  
				}  
			}
			
			kdAndKnList.add(nodeA);  
		}  

		return kdAndKnList;  
	}  
}

