# Machine Learning Demos
The content of this repository allows to run three machine learning demos. 

1. Clone this repo to a directory of your choice
2. Create the docker images for the demos
3. Run the demos

## Demo Quick Facts

- The three demos cover different areas of machine learning
  * Natural Language Processing
  * Pattern Recognition
  * Outlier Detection
- Each demo shows a different Java toolkit
  * [Mallet](http://mallet.cs.umass.edu/) 
  * [Deeplearning4j](https://deeplearning4j.org/) 
  * Standalone Java code and [Surus](https://github.com/Netflix/Surus)
- Everything is open and freely available
  * Machine learning toolkits
  * Data for examples
  * Related publications

## Repository Organisation

For each demo a different docker image setup is provided and the content of the repository is organized accordingly.

```
 +-- README.md (the file you are now reading)
 |
 +-- image_javadev
 |      +-- Dockerfile (parent dockerfile for java development)
 |
 +-- image_mallet
 |      +-- Dockerfile (setup for Mallet toolkit)
 |      +-- data
 |      |     +-- rt-polarity.* files (movie review data for sentiment analysis example)
 |      +-- papers
 |            +-- sentiment analysis paper
 |
 +-- image_deeplearning4j
 |      +-- Dockerfile (setup for deeplearning4j toolkit)
 |      +-- data
 |      |     +-- nist files (handwritten digits)
 |      +-- models
 |      |     +-- pretrained lenet5 deep neural net models at 1, 5, and 10 epochs of the training
 |      +-- papers
 |      |     +-- deep neural network papers: lenet, imagenet, facenet
 |      +-- dl4j-demo
 |            +-- ... maven project with demo code: LeNetMnistTrainer.java and LeNetMnistTester.java
 |      
 +-- image_outlier
        +-- Dockerfile (setup for outlier detection)
        +-- data
        |     +-- wdbc files (diagnostic breast cancer)
        +-- papers
        |     +-- robust local outlier factor (lof) and robust pca
        +-- outlier-demo
              +-- ... maven project with demo code LofTester.java

```

This has the benefit that the demos are independent of each other and allows for simple cleanup once the demos are no longer used.

## Create Docker Images

```
docker build --tag javadev:latest image_javadev
docker build --tag mallet:latest image_mallet
docker build --tag dl4j:latest image_deeplearning4j
docker build --tag outlier:latest image_outlier
```

## Sentiment Analysis with Mallet

Start a Mallet demo container.
```
docker run -it mallet:latest
```

Inside the container check out the data files
```
wc -l data/*.txt
head -7 data/rt-polarity.test.txt | tail -4 | cut -c -150
```

Import the data into the Mallet format and train a Naive Bayes classifier
```
mallet import-file --input data/rt-polarity.train.txt --output data.mallet.sentiment.train
mallet train-classifier --trainer NaiveBayes --training-portion 0.9 --input data.mallet.sentiment.train --output-classifier model.mallet.naivebayes
ls -l
```

Use the classifier to determine the sentiment of some new data
```
mallet classify-file --classifier model.mallet.naivebayes --input data/rt-polarity.test.txt --output - | head -7
head -7 data/rt-polarity.test.txt | cut -c -150
```

## Recognition of Handwritten Digits with Deeplearning4j

Start the deeplearning4j container
```
docker run -it dl4j:latest
```

Inside the container check out the data files
```
ls -l data/*
```

Training deep neural net takes hours. That's why there are some pretrained nets
```
ls -l models/*
```

Evaluate a model. First on 10,000 digits then on a single image
```
java -cp /usr/share/dl4j-demo/dl4j-demo.jar org.ece16.dl4j.LeNetMnistTester models/lenet.dl4j.epoch10.model
java -cp /usr/share/dl4j-demo/dl4j-demo.jar org.ece16.dl4j.LeNetMnistTester models/lenet.dl4j.epoch10.model 115
```

You can also train your own net
```
java -Xmx1024m -cp dl4j-demo.jar org.ece16.dl4j.LeNetMnistTrainer
```

## Detecting Breast Cancer with Custom Outlier Detection Code

Start the outlier container
```
docker run -it outlier:latest
```

Inside the container check out the data files
```
ls -l data/wdbc*
```

Search for outliers in the wdbc data with a varying value of k and thresold 2.4
```
java -cp /home/demo/outlier-demo/outlier-demo.jar org.ece16.lof.LofTester data/wdbc.data 2.4
```

Search for outliers in the wdbc data with k=40 and thresold 2.4
```
java -cp /home/demo/outlier-demo/outlier-demo.jar org.ece16.lof.LofTester data/wdbc.data 2.4 40
```
