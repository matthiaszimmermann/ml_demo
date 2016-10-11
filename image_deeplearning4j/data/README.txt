======================================================================
The MNIST data used for the deeplearning4j library is downloaded 
from the following URL:

    http://yann.lecun.com/exdb/mnist/

The data consists of both a training and a test set. For each set there
is an image file and a corresponding label file:

train-images-idx3-ubyte.gz:  training set images (9912422 bytes) 
train-labels-idx1-ubyte.gz:  training set labels (28881 bytes) 
t10k-images-idx3-ubyte.gz:   test set images (1648877 bytes) 
t10k-labels-idx1-ubyte.gz:   test set labels (4542 bytes)

In the deeplearning4j examples this data is loaded in class MnistFetcher:

	https://github.com/deeplearning4j/deeplearning4j/blob/master/deeplearning4j-core/src/main/java/org/deeplearning4j/base/MnistFetcher.java
	
======================================================================
