FROM javadev:latest

MAINTAINER Matthias Zimmermann <matthias.zimmermann@bsi-software.com>

# workaround to ensure access to internat from within container
RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf

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
    && mvn clean package
    
RUN cd $OUTLIER_DEMO_HOME \
	&& ln -s target/original-outlier-demo-$OUTLIER_DEMO_VERSION.jar $OUTLIER_DEMO_JAR
	
#--- finish up ---------------------------------------------------------------#

WORKDIR $DEMO_HOME

RUN echo "# try the outlier demo with the example below" > README.txt \
    && echo "java -cp $OUTLIER_DEMO_HOME/$OUTLIER_DEMO_JAR org.ece16.lof.LofTester data/wdbc.data 1" >> README.txt \
    && echo "java -cp $OUTLIER_DEMO_HOME/$OUTLIER_DEMO_JAR org.ece16.lof.LofTester data/wdbc.data 2" >> README.txt \
    && echo "java -cp $OUTLIER_DEMO_HOME/$OUTLIER_DEMO_JAR org.ece16.lof.LofTester data/wdbc.data 2.5" >> README.txt
