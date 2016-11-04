FROM javadev:latest

MAINTAINER Matthias Zimmermann <matthias.zimmermann@bsi-software.com>

# workaround to ensure access to internat from within container
RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf

ENV DEMO_HOME /home/demo
ENV DEMO_DATA $DEMO_HOME/data
ENV DEMO_MODELS $DEMO_HOME/models

RUN mkdir -p $DEMO_HOME

#--- prepare data and models -------------------------------------------------#

COPY models $DEMO_MODELS
COPY data $DEMO_DATA

RUN mkdir -p $HOME/MNIST \
    && cp $DEMO_DATA/train-images-idx3-ubyte.gz $HOME/MNIST/images-idx3-ubyte.gz \
    && cp $DEMO_DATA/train-labels-idx1-ubyte.gz $HOME/MNIST/labels-idx1-ubyte.gz \
    && cp $DEMO_DATA/t10k-images-idx3-ubyte.gz $HOME/MNIST/t10k-images-idx3-ubyte.gz \
    && cp $DEMO_DATA/t10k-labels-idx1-ubyte.gz $HOME/MNIST/t10k-labels-idx1-ubyte.gz \
	&& cd $HOME/MNIST \
	&& gunzip images-idx3-ubyte.gz \
	&& gunzip labels-idx1-ubyte.gz \
	&& gunzip t10k-images-idx3-ubyte \
	&& gunzip t10k-labels-idx1-ubyte.gz

#--- set up mnist lenet demo -------------------------------------------------#

ENV DL4J_PARENT /usr/share
ENV DL4J_DEMO_DIR dl4j-demo
ENV DL4J_DEMO_HOME $DL4J_PARENT/$DL4J_DEMO_DIR

ENV DL4J_DEMO_VERSION 1.0-SNAPSHOT
ENV DL4J_DEMO_JAR dl4j-demo.jar

RUN mkdir -p $DL4J_DEMO_HOME
COPY dl4j-demo $DL4J_DEMO_HOME

RUN cd $DL4J_DEMO_HOME \
    && mvn clean package \
	&& ln -s target/dl4j-demo-$DL4J_DEMO_VERSION-bin.jar $DL4J_DEMO_JAR

#--- set up official deeplearning4j examples ---------------------------------#

ENV DL4J_EXAMPLE_DIR dl4j-examples
ENV DL4J_EXAMPLE_HOME $DL4J_PARENT/$DL4J_EXAMPLE_DIR

ENV DL4J_EXAMPLE_VERSION 0.5-SNAPSHOT
ENV DL4J_EXAMPLE_JAR dl4j-example.jar

RUN cd $DL4J_PARENT \
    && git clone https://github.com/deeplearning4j/dl4j-examples.git \
	&& cd dl4j-examples \
	&& mvn clean package \
	&& ln -s dl4j-examples/target/dl4j-examples-$DL4J_EXAMPLE_VERSION-bin.jar $DL4J_EXAMPLE_JAR

#--- finish up ---------------------------------------------------------------#

WORKDIR $DEMO_HOME

RUN echo "# try the MNIST demo with the example below" > README.txt \
    && echo "java -cp $DL4J_DEMO_HOME/$DL4J_DEMO_JAR org.ece16.dl4j.LeNetMnistTester models/lenet.dl4j.epoch10.model 115" >> README.txt \
    && echo "java -cp $DL4J_EXAMPLE_HOME/$DL4J_EXAMPLE_JAR org.deeplearning4j.examples.feedforward.mnist.MLPMnistTwoLayerExample" >> README.txt
