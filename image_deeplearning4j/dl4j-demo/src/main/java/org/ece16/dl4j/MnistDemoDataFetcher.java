package org.ece16.dl4j;

import java.io.IOException;

import org.deeplearning4j.base.MnistFetcher;
import org.deeplearning4j.datasets.fetchers.MnistDataFetcher;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * MNIST fetcher class that allows to access single images by their index.
 */
public class MnistDemoDataFetcher extends MnistDataFetcher {
	
	public MnistDemoDataFetcher(boolean binarize, boolean train, boolean shuffle, int seed) throws IOException {
		super(binarize, train, shuffle, seed);
	}
	
	public String getFileNameImages(boolean train) {
		if(train) {
			return MNIST_ROOT + MnistFetcher.trainingFilesFilename_unzipped;
		}
		else {
			return MNIST_ROOT + MnistFetcher.testFilesFilename_unzipped;
		}
	}
	
	public String getFileNameLabels(boolean train) {
		if(train) {
			return MNIST_ROOT + MnistFetcher.trainingFileLabelsFilename_unzipped;
		}
		else {
			return MNIST_ROOT + MnistFetcher.testFileLabelsFilename_unzipped;
		}
	}
	
	public DataSet getSingleImage(int image_number) {
        float[][] featureData = new float[1][0];
        float[][] labelData = new float[1][0];
		 
        byte[] img = man.readImageUnsafe(image_number);
        int label = man.readLabel(image_number);
		
		float[] featureVec = new float[img.length];
        featureData[0] = featureVec;
        labelData[0] = new float[10];
        labelData[0][label] = 1.0f;
		
        for(int j=0; j < img.length; j++) {
            float v = ((int)img[j]) & 0xFF; //byte is loaded as signed -> convert to unsigned
				
            if(binarize){
                if(v > 30.0f) { featureVec[j] = 1.0f; }
			    else          { featureVec[j] = 0.0f; }
            } 
			else {
                featureVec[j] = v/255.0f;
            }
        }
		
        INDArray features = Nd4j.create(featureData);
        INDArray labels = Nd4j.create(labelData);

	    return new DataSet(features,labels);
	}
}