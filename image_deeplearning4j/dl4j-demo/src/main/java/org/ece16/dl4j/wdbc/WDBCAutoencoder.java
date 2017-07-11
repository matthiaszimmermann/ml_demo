package org.ece16.dl4j.wdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.FeedForwardLayer;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class WDBCAutoencoder {

	MultiLayerNetwork net;

	List<INDArray> featuresTrain;
	List<INDArray> featuresTest;
	List<INDArray> labelsTest;

	List<Triple<Double, Integer, INDArray>> scoredTest = new ArrayList<>();

	/**
	 * Constructor, needs pointer to the WDBC data file.
	 */
	public WDBCAutoencoder(String dataFileName, double trainingPercentage) {
		prepareData(dataFileName, trainingPercentage);
	}

	/**
	 * Builds and trains an auto-encoder for benign WDBC data.
	 * The auto-encoder is used as an anomaly detector 
	 * to find non-benign cases (=cancer).
	 * 
	 * WARNING: The original data is built for supervised learning. 
	 * Using this data for unsupervised learning is 
	 * for demonstration purposes only!
	 */
	public static void main(String[] args) {
		String dataFile = args[0];

		// train auto-encoder
		double trainingPercentage = 0.8;
		int intermediateDimensions = 14;
		int coreDimensions = 8;
		int epochs = 5;
		boolean detailedFalse = false;

		WDBCAutoencoder wdbc = new WDBCAutoencoder(dataFile, trainingPercentage);
		wdbc.train(intermediateDimensions, coreDimensions, epochs);
		wdbc.printTopology(detailedFalse);

		// evaluate auto-encoder
		double reconstructionErrorThreshold = 20_000;
		boolean debugTrue = true;

		wdbc.evaluate();
		wdbc.getResult(reconstructionErrorThreshold, debugTrue);

		boolean doGridSearch = false;
		if(doGridSearch) {
			wdbc.parameterGridSearch();
		}
	}

	/**
	 * Creates and trains an auto-encoder using the provided hyper-parameters.
	 */
	private void train(int intermediateDimensions, int coreDimensions, int epochs) {
		System.out.println();
		System.out.println("train auto-encoder ["+intermediateDimensions+", " + coreDimensions + "]");

		boolean addListenerFalse = false;
		createEncoder(intermediateDimensions, coreDimensions);
		train(epochs, addListenerFalse);
	}

	/**
	 * Reads the WDBC data from the provided file and reserves the specified 
	 * amount of data for training.
	 */
	private void prepareData(String dataFileName, double trainingPercentage) {

		featuresTrain = new ArrayList<>();
		featuresTest = new ArrayList<>();
		labelsTest = new ArrayList<>();

		int begninTestCases = 0;
		int malignTestCases = 0;
		int batchSize = 20;

		WDBCDataFetcher fetcher = new WDBCDataFetcher(dataFileName, WDBCDataFetcher.LABEL_BENIGN);
		DataSetIterator iter = new WDBCDatasetIterator(batchSize, fetcher);
		Random r = new Random(123456789); // for repeatability of results

		// process benign cases
		while(iter.hasNext()) {
			DataSet ds = iter.next();
			int trainSamples = (int)(ds.numExamples() * trainingPercentage);
			SplitTestAndTrain split = ds.splitTestAndTrain(trainSamples, r);
			featuresTrain.add(split.getTrain().getFeatureMatrix());

			// reserve some test cases
			DataSet dsTest = split.getTest();
			featuresTest.add(dsTest.getFeatureMatrix());
			INDArray indexes = Nd4j.argMax(dsTest.getLabels(), 1); //Convert from one-hot representation -> index
			labelsTest.add(indexes);
			begninTestCases += split.getTest().numExamples();
		}

		// add matching amount of malign examples to test set
		fetcher = new WDBCDataFetcher(dataFileName, WDBCDataFetcher.LABEL_MALIGN);
		iter = new WDBCDatasetIterator(batchSize, fetcher);

		while(iter.hasNext() && malignTestCases < begninTestCases) {
			DataSet ds = iter.next();
			featuresTest.add(ds.getFeatureMatrix());
			INDArray indexes = Nd4j.argMax(ds.getLabels(), 1); // Convert from one-hot representation -> index
			labelsTest.add(indexes);
			malignTestCases += ds.numExamples();
		}
	}

	/**
	 * 2D grid search over intermediate and core dimensions of auto-encoder network.
	 */
	private void parameterGridSearch() {
		String result;
		float fMeasure;
		float fMeasureMax = 0.0f;
		int epochs = 5;
		boolean addListenerFalse = false;

		// loop over dimension of intermediate encoding layer
		for(int intermediateDim = 26; intermediateDim >= 12; intermediateDim -= 2) {

			// loop over dimension of core encoding layer
			for(int coreDim = 2; coreDim <= 18 && coreDim < intermediateDim; coreDim += 2) {
				createEncoder(intermediateDim, coreDim);
				train(epochs, addListenerFalse);
				evaluate();
				result = evaluateForBestThreshold();

				fMeasure = Float.valueOf(result.substring(0, result.indexOf(' ')));

				System.out.print("" + intermediateDim+ " " + coreDim + " " + fMeasure);

				if(fMeasure > fMeasureMax) { System.out.println(" !! " + result); }
				else if(fMeasure == fMeasureMax) { System.out.println(" ! " + result); }
				else { System.out.println(); }

				fMeasureMax = Math.max(fMeasureMax, fMeasure);
			}
		}
	}

	/**
	 * Trains the auto-encoder over the specified number of epochs.
	 */
	private MultiLayerNetwork train(int epochs, boolean addListener) {

		if(addListener) {
			net.setListeners(new ScoreIterationListener(1));
		}

		for(int epoch = 0; epoch < epochs; epoch++ ){
			for(INDArray data : featuresTrain){
				net.fit(data, data);
			}
		}

		return net;
	}

	/**
	 * Set up network. 
	 * 30 in- and output dimensions (as WDBC data has 30 feature dimensions).
	 * 30 -> intermediate-dim -> core-dim -> intermediate-dim -> 30
	 */
	private MultiLayerNetwork createEncoder(int intermediateDimensions, int coreDimensions) {
		int inputDimensions = 30;
		int outputDimensions = inputDimensions;

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(12345)
				.iterations(1)
				.weightInit(WeightInit.XAVIER)
				.updater(Updater.ADAGRAD)
				.activation(Activation.RELU)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.learningRate(0.05)
				.regularization(true).l2(0.0001)
				.list()
				// 1st compression layer: input -> intermediate dimensionality
				.layer(0, new DenseLayer.Builder()
						.nIn(inputDimensions)
						.nOut(intermediateDimensions)
						.build())
				// 2nd compression layer: intermediate -> core dimensionality
				.layer(1, new DenseLayer.Builder()
						.nIn(intermediateDimensions)
						.nOut(coreDimensions)
						.build())
				// 1st expansion layer: core -> intermediate dimensionality
				.layer(2, new DenseLayer.Builder()
						.nIn(coreDimensions)
						.nOut(intermediateDimensions)
						.build())
				// 2nd expansion layer: intermediate -> output (=input) dimensionality
				.layer(3, new OutputLayer.Builder()
						.nIn(intermediateDimensions)
						.nOut(outputDimensions)
						.lossFunction(LossFunctions.LossFunction.MSE)
						.build())
				.pretrain(false)
				.backprop(true)
				.build();

		net = new MultiLayerNetwork(conf);

		return net;
	}

	private void evaluate() {
		scoredTest = new ArrayList<>();

		// run through all example chunks
		for(int i = 0; i < featuresTest.size(); i++) {
			INDArray testData = featuresTest.get(i);
			INDArray labels = labelsTest.get(i);
			int nRows = testData.rows();

			// go through each example individually
			for(int j = 0; j < nRows; j++) {
				INDArray example = testData.getRow(j);
				int digit = (int)labels.getDouble(j);
				double score = net.score(new DataSet(example,example));
				scoredTest.add(new ImmutableTriple<Double, Integer, INDArray>(new Double(score), new Integer(digit), example));
			}
		}

		// sort for increasing score
		Collections.sort(scoredTest, new Comparator<Triple<Double, Integer, INDArray>>() {
			@Override
			public int compare(Triple<Double, Integer, INDArray> o1, Triple<Double, Integer, INDArray> o2) {
				return(o1.getLeft().compareTo(o2.getLeft()));
			}
		});
	}

	private String evaluateForBestThreshold() {
		String bestResult = "";
		float fMeasureMax = 0.0f;
		boolean debugFalse = false;

		for(int i = 1; i < 20; i++) {
			float errorThreshold = (float) (i * 5000.0);
			String result = getResult(errorThreshold, debugFalse);
			float fMeasure = Float.valueOf(result.substring(0, result.indexOf(' ')));

			if(fMeasure >= fMeasureMax) {
				bestResult = result;
				fMeasureMax = fMeasure;
			}
		}

		return bestResult;
	}

	private void printTopology(boolean detailed) {
		System.out.println("\nauto-encoder topology:");

		if(detailed) {
			for(org.deeplearning4j.nn.api.Layer layer: net.getLayers()) {
				System.out.println(String.format("%s", layer.toString()));
			}
		}
		else {
			System.out.println(this.summary());
		}
	}

	private String getResult(double errorThreshold, boolean debug) {
		int truePositive = 0; // detects cancer and patient has cancer
		int falsePositive = 0; // detects cancer and patient is healthy
		int falseNegative = 0; // does not detect cancer but patient has cancer
		int trueNegative = 0; // does not detect cancer and patient is healthy

		for(Triple<Double, Integer, INDArray> sample: scoredTest) {
			float reconstructionError = sample.getLeft().floatValue();
			String label = sample.getMiddle().intValue() == 0 ? WDBCDataFetcher.LABEL_BENIGN : WDBCDataFetcher.LABEL_MALIGN;

			// "high" reconstruction error -> outlier -> malign diagnosis
			if(reconstructionError > errorThreshold) {
				if(label.equals(WDBCDataFetcher.LABEL_MALIGN)) {
					truePositive++;
				}
				else {
					falsePositive++;
				}
			}
			// "low" reconstruction error -> inside normal distribution -> begnin diagnosis
			else {
				if(label.equals(WDBCDataFetcher.LABEL_BENIGN)) {
					trueNegative++;
				}
				else {
					falseNegative++;
				}
			}
		}

		float precision = truePositive / (float)(truePositive + falsePositive);
		float recall = truePositive / (float)(truePositive + falseNegative);
		float fmeasure = 2 * precision * recall / (precision + recall);

		if(debug) {
			System.out.println();
			System.out.println("results for reconstruction error threshold " + errorThreshold);
			System.out.println();
			System.out.println(String.format("diagnosis | cancer | healthy |"));
			System.out.println(String.format("----------+--------+---------+"));
			System.out.println(String.format("positive  | %6d | %7d |", truePositive, falsePositive));
			System.out.println(String.format("negative  | %6d | %7d |", falseNegative, trueNegative));
			System.out.println(String.format("----------+--------+---------+"));
			System.out.println();
			System.out.println(String.format("precision: %.1f%% (probability for cancer given a positive diagnosis)", 100 * precision));
			System.out.println(String.format("recall:    %.1f%% (probability for positive diagnosis given cancer)", 100 * recall));
			System.out.println(String.format("f-measure: %.1f%% ", 100 * fmeasure));
		}

		return "" + fmeasure + " max-error " + errorThreshold + " precision " + precision + " recall " + recall +
				" [true-positive " + truePositive +" false-positive " + falsePositive + " false-negative " + falseNegative + " true-negative " + trueNegative + "]";
	}

	/**
	 * method copied/adapted from
	 * https://github.com/deeplearning4j/deeplearning4j/blob/master/deeplearning4j-nn/src/main/java/org/deeplearning4j/nn/graph/ComputationGraph.java
	 */
	public String summary() {
		String ret = "\n";
		ret += StringUtils.repeat("=", 70);
		ret += "\n";
		ret += String.format("%-40s%-15s%-15s\n", "VertexName (VertexType)", "nIn -> nOut", "Layer Params");
		ret += StringUtils.repeat("=", 70);
		ret += "\n";
		int totalParams = 0;

		for (int currentLayerIdx = 0; currentLayerIdx < net.getnLayers(); currentLayerIdx++) {
			Layer current = net.getLayer(currentLayerIdx).conf().getLayer();
			int layerParams = net.getLayer(currentLayerIdx).numParams();

			String name = String.valueOf(currentLayerIdx);
			String[] classNameArr = current.getClass().toString().split("\\.");
			String className = classNameArr[classNameArr.length - 1];

			String paramCount = "-";
			String in = "-";
			String out = "-";
			String paramShape = "-";
			if (current instanceof FeedForwardLayer) {
				FeedForwardLayer currentLayer = (FeedForwardLayer)current;
				classNameArr = currentLayer.getClass().getName().split("\\.");
				className = classNameArr[classNameArr.length - 1];
				paramCount = String.valueOf(layerParams);
				totalParams += layerParams;

				if (layerParams > 0) {
					paramShape = "";
					in = String.valueOf(currentLayer.getNIn());
					out = String.valueOf(currentLayer.getNOut());
					if(paramShape.lastIndexOf(",") >= 0) {
						paramShape = paramShape.subSequence(0, paramShape.lastIndexOf(",")).toString();
					}
				}
			}

			ret += String.format("%-40s%-15s%-15s", name + " (" + className + ")", in + " -> " + out, paramCount);
			ret += "\n";
		}

		ret += StringUtils.repeat("-", 70);
		ret += String.format("\n%30s %d", "Total Parameters: ", totalParams);
		ret += "\n";
		ret += StringUtils.repeat("=", 70);
		ret += "\n";

		return ret;
	}	
}
