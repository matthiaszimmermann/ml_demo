# Machine Learning Demos
Machine learning demos with Mallet, Deeplearning4j and Surus

1. Clone this repo to a directory of your choice
2. Create the docker images for the demos
3. Run the demos

## Create Docker Images

```
docker build --tag javadev:latest image_javadev
docker build --tag mallet:latest image_mallet
docker build --tag dl4j:latest image_deeplearning4j
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
java -cp dl4j-demo.jar org.ece16.dl4j.LeNetMnistTester models/lenet.dl4j.epoch10.model
java -cp dl4j-demo.jar org.ece16.dl4j.LeNetMnistTester models/lenet.dl4j.epoch10.model 115
```

You can also train your own net
```
java -Xmx1024m -cp dl4j-demo.jar org.ece16.dl4j.LeNetMnistTrainer
```
