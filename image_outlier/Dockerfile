FROM javadev:latest

MAINTAINER Matthias Zimmermann <matthias.zimmermann@bsi-software.com>

ENV DEMO_HOME /home/demo
ENV DEMO_DATA $DEMO_HOME/data

RUN mkdir -p $DEMO_HOME

#--- prepare data ------------------------------------------------------------#

COPY data $DEMO_DATA

#--- set up outlier demo -----------------------------------------------------#

ENV OUTLIER_DEMO_DIR outlier-demo
ENV OUTLIER_DEMO_HOME $DEMO_HOME/$OUTLIER_DEMO_DIR

ENV OUTLIER_DEMO_VERSION 1.0-SNAPSHOT
ENV OUTLIER_DEMO_JAR outlier-demo.jar

RUN mkdir -p $OUTLIER_DEMO_HOME
COPY outlier-demo $OUTLIER_DEMO_HOME

RUN cd $OUTLIER_DEMO_HOME \
    && mvn clean package \
	&& ln -s target/outlier-demo-$OUTLIER_DEMO_VERSION.jar $OUTLIER_DEMO_JAR
	
#--- finish up ---------------------------------------------------------------#

WORKDIR $DEMO_HOME

RUN echo "# try the outlier demo with the example below" > README.txt \
    && echo "java -cp $OUTLIER_DEMO_HOME/$OUTLIER_DEMO_JAR org.ece16.lof.LofTester data/wdbc.data 2.4" >> README.txt \
    && echo "java -cp $OUTLIER_DEMO_HOME/$OUTLIER_DEMO_JAR org.ece16.lof.LofTester data/wdbc.data 2.4 40" >> README.txt
