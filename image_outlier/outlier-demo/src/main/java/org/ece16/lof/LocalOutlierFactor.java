package org.ece16.lof;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    // 1.找到给定点与其他点的欧几里得距离  
    // 2.对欧几里得距离进行排序，找到前5位的点，并同时记下k距离  
    // 3.计算每个点的可达密度  
    // 4.计算每个点的局部离群点因子  
    // 5.对每个点的局部离群点因子进行排序，输出。  
    public List<DataNode> getOutlierNodes(List<DataNode> allNodes) {  
  
        List<DataNode> kdAndKnList = getKDAndKN(allNodes);  
        calReachDis(kdAndKnList);  
        calReachDensity(kdAndKnList);  
        calLof(kdAndKnList);  
        //降序排序  
        Collections.sort(kdAndKnList, new LofComparator());  
  
        return kdAndKnList;  
    }  
  
    /** 
     * 计算每个点的局部离群点因子 
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
     * 计算每个点的可达距离 
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
     * 计算每个点的可达密度,reachdis(p,o)=max{ k-distance(o),d(p,o)} 
     * @param kdAndKnList 
     */  
    private void calReachDis(List<DataNode> kdAndKnList) {  
        for (DataNode node : kdAndKnList) {  
            List<DataNode> tempNodes = node.getkNeighbor();  
            for (DataNode tempNode : tempNodes) {  
                //获取tempNode点的k-距离  
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
     * 计算给定点NodeA与其他点NodeB的欧几里得距离（distance）,并找到NodeA点的前5位NodeB，然后记录到NodeA的k-领域（kNeighbor）变量。 
     * 同时找到NodeA的k距离，然后记录到NodeA的k-距离（kDistance）变量中。 
     * 处理步骤如下： 
     * 1,计算给定点NodeA与其他点NodeB的欧几里得距离，并记录在NodeB点的distance变量中。 
     * 2,对所有NodeB点中的distance进行升序排序。 
     * 3,找到NodeB点的前5位的欧几里得距离点，并记录到到NodeA的kNeighbor变量中。 
     * 4,找到NodeB点的第5位距离，并记录到NodeA点的kDistance变量中。 
     * @param allNodes 
     * @return List<Node> 
     */  
    private List<DataNode> getKDAndKN(List<DataNode> allNodes) {  
        List<DataNode> kdAndKnList = new ArrayList<DataNode>();  
        for (int i = 0; i < allNodes.size(); i++) {  
            List<DataNode> tempNodeList = new ArrayList<DataNode>();  
            DataNode nodeA = new DataNode(allNodes.get(i));  
			
            //1,找到给定点NodeA与其他点NodeB的欧几里得距离，并记录在NodeB点的distance变量中。  
            for (int j = 0; j < allNodes.size(); j++) {  
                DataNode nodeB = new DataNode(allNodes.get(j));  
                //计算NodeA与NodeB的欧几里得距离(distance)  
                // double tempDis = getDis(nodeA, nodeB);
				double tempDis = nodeA.distance(nodeB);
                nodeB.setDistance(tempDis);  
                tempNodeList.add(nodeB);  
            }  
  
            //2,对所有NodeB点中的欧几里得距离（distance）进行升序排序。  
            Collections.sort(tempNodeList, new DistComparator());  
            for (int k = 1; k < INT_K; k++) {  
                //3,找到NodeB点的前5位的欧几里得距离点，并记录到到NodeA的kNeighbor变量中。  
                nodeA.getkNeighbor().add(tempNodeList.get(k));  
                if (k == INT_K - 1) {  
                    //4,找到NodeB点的第5位距离，并记录到NodeA点的kDistance变量中。  
                    nodeA.setkDistance(tempNodeList.get(k).getDistance());  
                }  
            }  
            kdAndKnList.add(nodeA);  
        }  
  
        return kdAndKnList;  
    }  
}
 
