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
 * Tester class for LocalOutlierFactor implementation.
 **/  
public class LofTester {  
	
    public static void main(String[] args) throws IOException {  
		
		if(args.length < 2 || args.length > 3) {
			System.err.println("usage: java org.ece16.lof.OutlierNodeDetect data-file threshold [k]");
			System.exit(1);
		}
          
		String dataFile = args[0];
		double threshold = Double.parseDouble(args[1]);
		int k = -1;
		
		if(args.length > 2) {
			k = Integer.parseInt(args[2]);
		}
        
        LocalOutlierFactor lof = new LocalOutlierFactor();
		BreastCancerWisconsinDataLoader loader = new BreastCancerWisconsinDataLoader(dataFile);		
        List<DataNode> dataNodes = loader.getDataNodes();
        List<DataNode> nodesSorted = null;
		
		if(k > 0) {
            lof.setK(k);
            nodesSorted = lof.getOutlierNodes(dataNodes); 				 
            printEval(k, nodesSorted, threshold, false);
		}
		else {
            int[] kArray = {10,20,30,40,50};
            for (int k_i: kArray){
                lof.setK(k_i);
                nodesSorted = lof.getOutlierNodes(dataNodes); 				 
		        printEval(k_i, nodesSorted, threshold, true);
            }
		}
		
		System.exit(0);
    }
	
	/**
	 * Prints some standard metrics for task.
	 * https://en.wikipedia.org/wiki/F1_score
	 */
	private static void printEval(int k, List<DataNode> nodes, double threshold, boolean shortFormat) {
		int mCorrect = 0;
		int mFalseAlarm = 0;
		int bCorrect = 0;
		int bFalseAlarm = 0;
		
		for(DataNode node: nodes) {
			if(node.getNodeLabel().equals("M")) {
				if(node.getLof() > threshold) { mCorrect++; }
				else                          { mFalseAlarm++; }
			}
			else {
				if(node.getLof() <= threshold) { bCorrect++; }
				else                           { bFalseAlarm++; }
			}
		}
				
	    double precision = (double)(1.0 * mCorrect/(mCorrect + mFalseAlarm));
	    double recall = (double)(1.0 * mCorrect/(mCorrect + bFalseAlarm));
	    double f1 = 2.0 * (precision * recall) / (precision + recall);

		if(shortFormat) {
		  printf("k %d f1 %.3f (recall %.3f precision %.3f)\n", k, f1, recall, precision);
		}
		else {
		    printf("=== data points (sort: lof, decending order)\n", k);
			printNodes(nodes, threshold);
			
			println();
		    printf("===[k=%d threshold=%.2f]===\n", k, threshold);
		    printf("mCorrect    %3d mFalseAlarm %3d\n", mCorrect, mFalseAlarm);
		    printf("bFalseAlarm %3d bCorrect    %3d\n", bFalseAlarm, bCorrect);
		
		    printf("---\n");
		    printf("f1 %.3f (recall %.3f precision %.3f)\n", f1, recall, precision);
		}
	}
	
	private static void printNodes(List<DataNode> nodes, double threshold) {
        for (DataNode node : nodes) {
			String name = node.getNodeName();
			String label = node.getNodeLabel();
			double lof = node.getLof();
			
			printf("sample %09d %s lof= %.4f ", Integer.parseInt(name), label, lof);
			
			if(lof > threshold && label.equals("M")) { print("true positive"); }
			if(lof > threshold && label.equals("B")) { print("false positive"); }
			
			if(lof <= threshold && label.equals("M")) { print("false negative"); }
			if(lof <= threshold && label.equals("B")) { print("true negative"); }
			
			print("\n");
        }
	}
	
	private static void printf(String f, Object... args) {
		print(String.format(f, args));
	}
	
	private static void println() {
		print("\n");
	}
	
	private static void print(Object o) {
		System.out.print(o);
	}
}
 
