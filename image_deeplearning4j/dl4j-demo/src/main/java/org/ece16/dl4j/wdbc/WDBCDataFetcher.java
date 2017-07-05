package org.ece16.dl4j.wdbc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.deeplearning4j.datasets.fetchers.BaseDataFetcher;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * https://archive.ics.uci.edu/ml/datasets/breast+cancer+wisconsin+(original)
 */
public class WDBCDataFetcher extends BaseDataFetcher {

	private static final long serialVersionUID = 1L;

	public static final String LABEL_BENIGN= "B";
	public static final String LABEL_MALIGN= "M";
	public static final String LABEL_ALL= "A";

	public static final String DEFAULT_DATA_PATH = "data/wdbc.data";
	public static final double threshold = 0.1;

	private String [] ids = null;
	private String [] labels = null;
	private float [][] features = null;

	@Override
	public void fetch(int numExamples) {
		List<float []> featureList = new ArrayList<float[]>();
		List<float []> labelList = new ArrayList<float[]>();

		int examplesRead = 0;

		for (; examplesRead < numExamples; examplesRead++) {
			// check that we still have more data
			if (cursor + examplesRead >= totalExamples) {
				break;
			}

			int index = cursor + examplesRead;
			featureList.add(features[index]);
			labelList.add(toLabelArray(labels[index]));
		}

		// update current example data
		INDArray featureArray = Nd4j.create(toFloat2DArray(featureList));
		INDArray labelArray = Nd4j.create(toFloat2DArray(labelList));
		curr = new DataSet(featureArray, labelArray);

		// update cursor value
		cursor += examplesRead;
	}
	
	/**
	 * Converts provided label into one hot encoded float array.
	 */
	private float[] toLabelArray(String label) {
		float[] labels = new float[2];

		if(LABEL_BENIGN.equals(label)) { labels[0] = 1F; }
		else                           { labels[1] = 1F; }

		return labels;
	}

	private float[][] toFloat2DArray(List<float[]> features) {
		int n = features.size();
		float [][] featuresArray = new float[n][];

		for(int i = 0; i < n; i++) {
			featuresArray[i] = features.get(i);
		}

		return featuresArray;
	}

	public WDBCDataFetcher(String filename, String labelFilter) {
		try {
			initFromFile(filename, labelFilter);
			numOutcomes = 2; // 'benign' and 'malign'
			totalExamples =  features.length;
			cursor = 0;
		} 
		catch (Exception e) {
			System.err.println("error during loading file='" + filename + "', filter=" + labelFilter);
		}
	}

	public String getId(int i) {
		return ids[i];
	}

	public String getLabel(int i) {
		return labels[i];
	}

	public float[] getFeatures(int i) {
		return features[i];
	}
	
	public INDArray getFeaturesAsINDArray(int i) {
		return floatToINDArray(getFeatures(i));
	}
	
	private INDArray floatToINDArray(float [] features) {
		float [][] featuresArray = new float[1][];
		featuresArray[0] = features;
		return Nd4j.create(featuresArray);
	}	

	public void initFromFile(String filename, String labelFilter) throws IOException {

		List<String> filteredIds = new ArrayList<>();
		List<String> filteredLabels = new ArrayList<>();
		List<float[]> filteredFeatures = new ArrayList<>();

		List<String> lines = readLines(filename);
		for(String line: lines) {
			String [] cells = line.trim().split(",");
			String id = cells[0];
			String label = cells[1];

			if(LABEL_ALL.equals(labelFilter) || label.equals(labelFilter)) {
				filteredIds.add(id);
				filteredLabels.add(label);
				filteredFeatures.add(cellsToFeatureVector(cells));
			}
		}

		ids = filteredIds.toArray(new String [totalExamples]);
		labels =  filteredLabels.toArray(new String [totalExamples]);
		features = filteredFeatures.toArray(new float [totalExamples][]);
		totalExamples = ids.length;
	}

	private float[] cellsToFeatureVector(String[] cells) {
		float [] values = new float[cells.length - 2];

		for(int col = 0; col < cells.length - 2; col++) {
			values[col] = Float.parseFloat(cells[col + 2]);
		}

		return values;
	}

	protected static List<String> readLines(String filename) throws IOException {
		List<String> list = new ArrayList<>();
		Scanner s = new Scanner(new File(filename));

		while (s.hasNextLine()){
			list.add(s.nextLine());
		}

		s.close();

		return list;
	}

	public static void main(String [] args) throws IOException {
		WDBCDataFetcher fetcher = new WDBCDataFetcher(args[0], args[1]);

		for(int row = 0; row < fetcher.totalExamples(); row++) {
			printf(fetcher.toString(row));
			println();
		}
		
		printf("--- summary ---\n");
		printf("examples: %d\n", fetcher.totalExamples());
		printf("feature dimension: %d\n", fetcher.getFeatures(0).length);
		printf("---------------\n");
	}

	public String toString(int i) {
		StringBuffer buf = new StringBuffer("example[" + i + "]={");
		buf.append(String.format("id=\"%s\", ", getId(i)));
		buf.append(String.format("label=\"%s\", ", getLabel(i)));
		buf.append("features=[");

		float [] data = getFeatures(i);
		for(int j = 0; j < data.length; j++) {
			if(j < data.length - 1) {
				buf.append(String.format("%f, ", data[j]));
			}
			else { 
				buf.append(String.format("%f", data[j]));
			}
		}

		buf.append("]}");

		return buf.toString();
	}


	private static void println() {
		printf("\n");
	}

	private static void printf(String f, Object... args) {
		print(String.format(f, args));
	}

	private static void print(Object o) {
		System.out.print(o);
	}

}
