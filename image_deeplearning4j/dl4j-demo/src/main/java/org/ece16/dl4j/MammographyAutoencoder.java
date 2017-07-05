package org.ece16.dl4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.ece16.dl4j.wdbc.WDBCDataFetcher;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * WARNING this is copy paste code from the WDBC demo.
 * Not optimized and hardly tested.
 */
public class MammographyAutoencoder {

	public static void main(String[] args) {

		// prepare training and test data (should also use validation data to be more realistic)
		String fileName = args[0];
		String labelFilter = MammographyDataFetcher.LABEL_BENIGN;
		MammographyDataFetcher fetcher = new MammographyDataFetcher(fileName, labelFilter);

		int batchSize = 100;
		DataSetIterator iter = new MammographyDatasetIterator(batchSize, fetcher);

		List<INDArray> featuresTrain = new ArrayList<>();
		List<INDArray> featuresTest = new ArrayList<>();
		List<INDArray> labelsTest = new ArrayList<>();

		// process benign cases
		int begninTestCases = 0;
		//		Random r = new Random(12345);
		Random r = new Random();
		while(iter.hasNext()) {
			DataSet ds = iter.next();
			int trainSamples = (int)(ds.numExamples() * 0.80); // use 80% of examples for training
			SplitTestAndTrain split = ds.splitTestAndTrain(trainSamples, r);
			featuresTrain.add(split.getTrain().getFeatureMatrix());
			DataSet dsTest = split.getTest();
			featuresTest.add(dsTest.getFeatureMatrix());
			INDArray indexes = Nd4j.argMax(dsTest.getLabels(), 1); //Convert from one-hot representation -> index
			labelsTest.add(indexes);

			begninTestCases += split.getTest().numExamples();
		}

		// add malign cases to test set
		int malignTestCases = 0;

		labelFilter = MammographyDataFetcher.LABEL_MALIGN;
		fetcher = new MammographyDataFetcher(fileName, labelFilter);
		iter = new MammographyDatasetIterator(batchSize, fetcher);

		while(iter.hasNext() && malignTestCases < begninTestCases) {
			DataSet ds = iter.next();
			featuresTest.add(ds.getFeatureMatrix());
			INDArray indexes = Nd4j.argMax(ds.getLabels(), 1); // Convert from one-hot representation -> index
			labelsTest.add(indexes);

			malignTestCases += ds.numExamples();
		}

		// 2D grid search over intermediate and core dimensions of auto-encoder network
		String result;
		float fMeasure;
		float fMeasureMax = 0.0f;
		for(int intermediateDim = 5; intermediateDim >= 3; intermediateDim--) {
			for(int coreDim = 2; coreDim <= 4 && coreDim < intermediateDim; coreDim++) {
				MultiLayerNetwork net = createNet(intermediateDim, coreDim);
				net = trainNet(net, featuresTrain);

				result = evaluateNet(net, featuresTest, labelsTest);
				fMeasure = Float.valueOf(result.substring(0, result.indexOf(' ')));

				System.out.print("" + intermediateDim+ " " + coreDim + " " + fMeasure);

				if(fMeasure > fMeasureMax) { System.out.println(" !! " + result); }
				else if(fMeasure == fMeasureMax) { System.out.println(" ! " + result); }
				else { System.out.println(); }

				fMeasureMax = Math.max(fMeasureMax, fMeasure);
			}
		}
	}

	private static MultiLayerNetwork trainNet(MultiLayerNetwork net, List<INDArray> featuresTrain) {
		int nEpochs = 5;
		for( int epoch = 0; epoch < nEpochs; epoch++ ){
			for(INDArray data : featuresTrain){
				net.fit(data, data);
			}
		}

		return net;
	}

	/**
	 * Set up network. 
	 * 6 in- and output dimensions (as mammography data has 6 feature dimensions).
	 * 6 -> intermediate-dim -> core-dim -> intermediate-dim -> 6
	 */
	private static MultiLayerNetwork createNet(int intermediateDimensions, int coreDimensions) {
		int inputDimensions = 6;
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
				.layer(0, new DenseLayer.Builder()
						.nIn(inputDimensions)
						.nOut(intermediateDimensions)
						.build())
				.layer(1, new DenseLayer.Builder()
						.nIn(intermediateDimensions)
						.nOut(coreDimensions)
						.build())
				.layer(2, new DenseLayer.Builder()
						.nIn(coreDimensions)
						.nOut(intermediateDimensions)
						.build())
				.layer(3, new OutputLayer.Builder()
						.nIn(intermediateDimensions)
						.nOut(outputDimensions)
						.lossFunction(LossFunctions.LossFunction.MSE)
						.build())
				.pretrain(false)
				.backprop(true)
				.build();

		return new MultiLayerNetwork(conf);
	}

	private static String evaluateNet(MultiLayerNetwork net, List<INDArray> featuresTest, List<INDArray> labelsTest) {
		List<Triple<Double, Integer, INDArray>> scoredList = new ArrayList<>();

		// run throug all example chunks
		for(int i = 0; i < featuresTest.size(); i++) {
			INDArray testData = featuresTest.get(i);
			INDArray labels = labelsTest.get(i);
			int nRows = testData.rows();

			// go through each example individually
			for(int j = 0; j < nRows; j++) {
				INDArray example = testData.getRow(j);
				int digit = (int)labels.getDouble(j);
				double score = net.score(new DataSet(example,example));
				scoredList.add(new ImmutableTriple<Double, Integer, INDArray>(new Double(score), new Integer(digit), example));
			}
		}

		// sort for increasing score
		Collections.sort(scoredList, new Comparator<Triple<Double, Integer, INDArray>>() {
			@Override
			public int compare(Triple<Double, Integer, INDArray> o1, Triple<Double, Integer, INDArray> o2) {
				return(o1.getLeft().compareTo(o2.getLeft()));
			}
		});

		// grid search for error threshold (maximizes f-measure)
		String bestResult = "";
		float fMeasureMax = 0.0f;

		for(int i = 1; i < 30; i++) {
			float errorThreshold = (float) (i * 0.05);
			String result = evaluateForThreshold(errorThreshold, scoredList);
			float fMeasure = Float.valueOf(result.substring(0, result.indexOf(' ')));

			System.out.println(result);
			
			if(fMeasure >= fMeasureMax) {
				bestResult = result;
				fMeasureMax = fMeasure;
			}
		}

		return bestResult;
	}

	private static String evaluateForThreshold(float errorThreshold, List<Triple<Double, Integer, INDArray>> list) {
		int truePositive = 0; // detects cancer and patient has cancer
		int falsePositive = 0; // detects cancer and patient is healthy
		int falseNegative = 0; // does not detect cancer but patient has cancer
		int trueNegative = 0; // does not detect cancer and patient is healthy

		for(Triple<Double, Integer, INDArray> sample: list) {
			float predictionError = sample.getLeft().floatValue();
			String label = sample.getMiddle().intValue() == 0 ? WDBCDataFetcher.LABEL_BENIGN : WDBCDataFetcher.LABEL_MALIGN;
			//        	System.out.println(String.format("%.2f, %s", predictionError, label));

			if(predictionError > errorThreshold) {
				if(label.equals(WDBCDataFetcher.LABEL_MALIGN)) {
					truePositive++;
				}
				else {
					falsePositive++;
				}
			}
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

		return "" + fmeasure + " max-error " + errorThreshold + " precision " + precision + " recall " + recall +
				" [true-positive " + truePositive +" false-positive " + falsePositive + " false-negative " + falseNegative + " true-negative " + trueNegative + "]";
	}
}
