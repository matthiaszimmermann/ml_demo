FROM javadev:latest

MAINTAINER Matthias Zimmermann <matthias.zimmermann@bsi-software.com>

# workaround to ensure access to internat from within container
RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf

ENV DEMO_HOME /home/demo
ENV DEMO_DATA $DEMO_HOME/data

RUN mkdir -p $DEMO_HOME

#--- prepare data and models -------------------------------------------------#

COPY data $DEMO_DATA

#--- prepare mallet package --------------------------------------------------#

ENV MALLET_HOME /usr/share/mallet

RUN mkdir -p $MALLET_HOME \
    && cd $MALLET_HOME \
    && git clone https://github.com/mimno/Mallet.git \
	&& cd Mallet \
	&& ant jar

ENV PATH $PATH:$MALLET_HOME/Mallet/bin

#--- finish up ---------------------------------------------------------------#

WORKDIR $DEMO_HOME

RUN echo "# try to test the mallet installation with the example below" > README.txt \
	&& echo "# language analysis" >> README.txt \
    && echo "mallet import-dir --input /usr/share/mallet/Mallet/sample-data/web/* --output web.mallet" >> README.txt \
    && echo "mallet train-classifier --input web.mallet --output-classifier my.classifier --trainer C45 --training-portion 0.9" >> README.txt \
	&& echo "# sentiment analysis" >> README.txt \
    && echo "mallet import-file --input data/rt-polarity.train.txt --output data.mallet.sentiment.train" >> README.txt \
    && echo "mallet train-classifier --trainer NaiveBayes --training-portion 0.9 --input data.mallet.sentiment.train --output-classifier model.mallet.naivebayes" >> README.txt \
    && echo "mallet classify-file --classifier model.mallet.naivebayes --input data/rt-polarity.test.txt --output - | head -7" >> README.txt
	