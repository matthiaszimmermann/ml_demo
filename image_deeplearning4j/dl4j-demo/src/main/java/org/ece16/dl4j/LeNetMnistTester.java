package org.ece16.dl4j;

import java.io.File;
import java.util.Scanner;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;

import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tester class for LeNet.
 * Code adapted from {@linktourl https://github.com/deeplearning4j/dl4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/convolution/LenetMnistExample.java}
 */
public class LeNetMnistTester {
    private static final Logger log = LoggerFactory.getLogger(LeNetMnistTester.class);

    public static void main(String[] args) throws Exception {
		
		if(args.length < 1 || args.length > 2) {
			System.err.println("usage: java org.ece16.dl4j.LeNetMnistTester model-file [image-number]");
			System.exit(1);
		}
		
		String leNetModelFileName = args[0];
		File leNetModelFile = new File(leNetModelFileName);
		
		log.info("Load model from file '" + leNetModelFile.getAbsolutePath() + "'");
		MultiLayerNetwork leNetModel = ModelSerializer.restoreMultiLayerNetwork(leNetModelFile);
		
		if(args.length == 1) {
			evalMnistTestSet(leNetModel);
		}
		else {
			boolean binarize = false;
			boolean train = false; // go for the MNIST test set
			boolean shuffle = false; // use original ordering
			int seed = 0;
			
			log.info("Load test data....");
			MnistDemoDataFetcher dataFetcher = new MnistDemoDataFetcher(binarize, train, shuffle, seed);
			int imageNumber = Integer.parseInt(args[1]);
			Scanner scanner = new Scanner(System.in);
			String [] evalOutput = new String[12];
			
			log.info("Evaluate model on individual images");
			while(imageNumber >= 0 && imageNumber < dataFetcher.totalExamples()) {				
				DataSet dataSet = dataFetcher.getSingleImage(imageNumber);
				INDArray features = dataSet.getFeatures();
				INDArray labels = dataSet.getLabels();
				INDArray modelOutput = leNetModel.output(features, false);
				int imageLabel = 0;
				int modelLabel = 0;
				double maxModelValue = 0.0;
				
				for(int i = 0; i < labels.columns(); i++) {
					evalOutput[i] = "label: " + i + " target: " + labels.getDouble(0,i) + " model: " + modelOutput.getDouble(0,i);
					
					if(labels.getDouble(0,i) > 0.5) {
						imageLabel = i;
					}
					
					if(modelOutput.getDouble(0,i) > maxModelValue) {
						modelLabel = i;
						maxModelValue = modelOutput.getDouble(0,i);
					}
				}
				
				evalOutput[10] = new String();
				evalOutput[11] = String.format("LeNet: %d (%f)", modelLabel, maxModelValue);
				
				renderImageAndOutput(imageNumber, imageLabel, features, evalOutput);
				
				System.out.print(String.format("Next image [0..%d): ", dataFetcher.totalExamples()));
				
				if(scanner.hasNextInt()) {
					imageNumber = scanner.nextInt();
				}
				else {
					imageNumber = -1;
				}				
			}
		}
    }
	
	private static void renderImageAndOutput(int imageNumber, int imageLabel, INDArray features, String [] evalOutput) {
		log.info(String.format("+---[ Label: %s Id: %05d ]---+", imageLabel, imageNumber));
		
		int rows = 28; // hard coded MNIST resolution
		int cols = 28; // hard coded MNIST resolution
		int idx = 0;

		for (int row = 0; row < rows; row++) {
			StringBuffer sb = new StringBuffer();
			sb.append("|");
			
			for (int col = 0; col < cols; col++) {
				int pixelVal = (int)(255.0f * features.getFloat(0, idx++));
				if (pixelVal == 0) {
					sb.append(" ");
				}
				else if (pixelVal < 256 / 3) {
					sb.append(".");
				}
				else if (pixelVal < 2 * (256 / 3)) {
					sb.append("x");
				}
				else {
					sb.append("X");
				}
			}
			
			sb.append("| ");
			
			if(row < evalOutput.length) {
				log.info(String.format("%s %s", sb.toString(), evalOutput[row]));
			}
			else {
				log.info(sb.toString());
			}
		}
		
		log.info("+----------------------------+");
	}
	
	private static void evalMnistTestSet(MultiLayerNetwork leNetModel) throws Exception {
		
        log.info("Load test data....");
        int batchSize = 64;
        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize,false,12345);
		
        log.info("Evaluate model....");
        int outputNum = 10;
        Evaluation eval = new Evaluation(outputNum);
		
        while(mnistTest.hasNext()){
            DataSet dataSet = mnistTest.next();
            INDArray output = leNetModel.output(dataSet.getFeatureMatrix(), false);
            eval.eval(dataSet.getLabels(), output);
        }
		
        log.info(eval.stats());
	}
}