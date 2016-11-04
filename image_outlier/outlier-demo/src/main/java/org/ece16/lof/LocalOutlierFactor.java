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
			//return A.getDistance() - B.getDistance() < 0 ? -1 : 1;  
			if((A.getDistance()-B.getDistance())<0)     
				return -1;    
			else if((A.getDistance()-B.getDistance())>0)    
				return 1;    
			else return 0;    
		}  
	}  

	/** 
	 * Comparator for sorting DataNode using the LOF value in descending order.
	 */  
	public class LofComparator implements Comparator<DataNode> {  
		public int compare(DataNode A, DataNode B) {  
			//return A.getLof() - B.getLof() < 0 ? 1 : -1;  
			if((A.getLof()-B.getLof())<0)     
				return 1;    
			else if((A.getLof()-B.getLof())>0)    
				return -1;    
			else return 0;    
		}  
	}    

	/**
	 * Computes LOF values for all nodes and retuns a sorted set of the nodes.
	 */
	// 1.æ‰¾åˆ°ç»™å®šç‚¹ä¸Žå…¶ä»–ç‚¹çš„æ¬§å‡ é‡Œå¾—è·�ç¦»  
	// 2.å¯¹æ¬§å‡ é‡Œå¾—è·�ç¦»è¿›è¡ŒæŽ’åº�ï¼Œæ‰¾åˆ°å‰�5ä½�çš„ç‚¹ï¼Œå¹¶å�Œæ—¶è®°ä¸‹kè·�ç¦»  
	// 3.è®¡ç®—æ¯�ä¸ªç‚¹çš„å�¯è¾¾å¯†åº¦  
	// 4.è®¡ç®—æ¯�ä¸ªç‚¹çš„å±€éƒ¨ç¦»ç¾¤ç‚¹å› å­�  
	// 5.å¯¹æ¯�ä¸ªç‚¹çš„å±€éƒ¨ç¦»ç¾¤ç‚¹å› å­�è¿›è¡ŒæŽ’åº�ï¼Œè¾“å‡ºã€‚  
	public List<DataNode> getOutlierNodes(List<DataNode> allNodes) {  

		List<DataNode> kdAndKnList = getKDAndKN(allNodes);  
		calReachDis(kdAndKnList);  
		calReachDensity(kdAndKnList);  
		calLof(kdAndKnList);  
		//é™�åº�æŽ’åº�  
		Collections.sort(kdAndKnList, new LofComparator());  

		return kdAndKnList;  
	}  

	/** 
	 * è®¡ç®—æ¯�ä¸ªç‚¹çš„å±€éƒ¨ç¦»ç¾¤ç‚¹å› å­� 
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
	 * è®¡ç®—æ¯�ä¸ªç‚¹çš„å�¯è¾¾è·�ç¦» 
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
	 * è®¡ç®—æ¯�ä¸ªç‚¹çš„å�¯è¾¾å¯†åº¦,reachdis(p,o)=max{ k-distance(o),d(p,o)} 
	 * @param kdAndKnList 
	 */  
	private void calReachDis(List<DataNode> kdAndKnList) {  
		for (DataNode node : kdAndKnList) {  
			List<DataNode> tempNodes = node.getkNeighbor();  
			for (DataNode tempNode : tempNodes) {  
				//èŽ·å�–tempNodeç‚¹çš„k-è·�ç¦»  
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
	 * è®¡ç®—ç»™å®šç‚¹NodeAä¸Žå…¶ä»–ç‚¹NodeBçš„æ¬§å‡ é‡Œå¾—è·�ç¦»ï¼ˆdistanceï¼‰,å¹¶æ‰¾åˆ°NodeAç‚¹çš„å‰�5ä½�NodeBï¼Œç„¶å�Žè®°å½•åˆ°NodeAçš„k-é¢†åŸŸï¼ˆkNeighborï¼‰å�˜é‡�ã€‚ 
	 * å�Œæ—¶æ‰¾åˆ°NodeAçš„kè·�ç¦»ï¼Œç„¶å�Žè®°å½•åˆ°NodeAçš„k-è·�ç¦»ï¼ˆkDistanceï¼‰å�˜é‡�ä¸­ã€‚ 
	 * å¤„ç�†æ­¥éª¤å¦‚ä¸‹ï¼š 
	 * 1,è®¡ç®—ç»™å®šç‚¹NodeAä¸Žå…¶ä»–ç‚¹NodeBçš„æ¬§å‡ é‡Œå¾—è·�ç¦»ï¼Œå¹¶è®°å½•åœ¨NodeBç‚¹çš„distanceå�˜é‡�ä¸­ã€‚ 
	 * 2,å¯¹æ‰€æœ‰NodeBç‚¹ä¸­çš„distanceè¿›è¡Œå�‡åº�æŽ’åº�ã€‚ 
	 * 3,æ‰¾åˆ°NodeBç‚¹çš„å‰�5ä½�çš„æ¬§å‡ é‡Œå¾—è·�ç¦»ç‚¹ï¼Œå¹¶è®°å½•åˆ°åˆ°NodeAçš„kNeighborå�˜é‡�ä¸­ã€‚ 
	 * 4,æ‰¾åˆ°NodeBç‚¹çš„ç¬¬5ä½�è·�ç¦»ï¼Œå¹¶è®°å½•åˆ°NodeAç‚¹çš„kDistanceå�˜é‡�ä¸­ã€‚ 
	 * @param allNodes 
	 * @return List<Node> 
	 */  
	private List<DataNode> getKDAndKN(List<DataNode> allNodes) {  
		List<DataNode> kdAndKnList = new ArrayList<DataNode>();  
		for (int i = 0; i < allNodes.size(); i++) {  
			List<DataNode> tempNodeList = new ArrayList<DataNode>();  
			DataNode nodeA = new DataNode(allNodes.get(i));  

			//1,æ‰¾åˆ°ç»™å®šç‚¹NodeAä¸Žå…¶ä»–ç‚¹NodeBçš„æ¬§å‡ é‡Œå¾—è·�ç¦»ï¼Œå¹¶è®°å½•åœ¨NodeBç‚¹çš„distanceå�˜é‡�ä¸­ã€‚  
			for (int j = 0; j < allNodes.size(); j++) {  
				DataNode nodeB = new DataNode(allNodes.get(j));  
				//è®¡ç®—NodeAä¸ŽNodeBçš„æ¬§å‡ é‡Œå¾—è·�ç¦»(distance)  
				// double tempDis = getDis(nodeA, nodeB);
				double tempDis = nodeA.distance(nodeB);
				nodeB.setDistance(tempDis);  
				tempNodeList.add(nodeB);  
			}  

			//2,å¯¹æ‰€æœ‰NodeBç‚¹ä¸­çš„æ¬§å‡ é‡Œå¾—è·�ç¦»ï¼ˆdistanceï¼‰è¿›è¡Œå�‡åº�æŽ’åº�ã€‚  
			Collections.sort(tempNodeList, new DistComparator());  
			for (int k = 1; k < INT_K; k++) {  
				//3,æ‰¾åˆ°NodeBç‚¹çš„å‰�5ä½�çš„æ¬§å‡ é‡Œå¾—è·�ç¦»ç‚¹ï¼Œå¹¶è®°å½•åˆ°åˆ°NodeAçš„kNeighborå�˜é‡�ä¸­ã€‚  
				nodeA.getkNeighbor().add(tempNodeList.get(k));  
				if (k == INT_K - 1) {  
					//4,æ‰¾åˆ°NodeBç‚¹çš„ç¬¬5ä½�è·�ç¦»ï¼Œå¹¶è®°å½•åˆ°NodeAç‚¹çš„kDistanceå�˜é‡�ä¸­ã€‚  
					nodeA.setkDistance(tempNodeList.get(k).getDistance());  
				}  
			}  
			kdAndKnList.add(nodeA);  
		}  

		return kdAndKnList;  
	}  
}

