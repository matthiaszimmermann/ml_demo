FROM java:openjdk-8

MAINTAINER Matthias Zimmermann <matthias.zimmermann@bsi-software.com>

# workaround to ensure access to internat from within container
RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

# dockerfile original source:
# https://github.com/dockunit/docker-prebuilt/blob/master/java-maven-ant-gradle/openjdk-8-jdk/Dockerfile

#--- maven installation ------------------------------------------------------#

ENV MAVEN_VERSION 3.3.3

RUN curl -fsSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
  && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven

# create a java hello world to download mvn standard packages into javadev image
RUN cd \
	&& mvn archetype:generate -DgroupId=com.github.matthiaszimmermann.helloworld -DartifactId=helloworld -DinteractiveMode=false \
	&& cd helloworld \
	&& mvn clean install

#--- gradle installation -----------------------------------------------------#

ENV GRADLE_VERSION 2.8
ENV GRADLE_HASH fe801ce2166e6c5b48b3e7ba81277c41
WORKDIR /usr/lib
RUN wget https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
    && unzip "gradle-${GRADLE_VERSION}-bin.zip" \
    && ln -s "/usr/lib/gradle-${GRADLE_VERSION}/bin/gradle" /usr/bin/gradle \
    && rm "gradle-${GRADLE_VERSION}-bin.zip"

ENV GRADLE_HOME /usr/src/gradle
ENV PATH $PATH:$GRADLE_HOME/bin

#--- ant installation --------------------------------------------------------#

ENV ANT_VERSION 1.9.4
RUN cd && \
    wget -q http://archive.apache.org/dist/ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz && \
    tar -xzf apache-ant-${ANT_VERSION}-bin.tar.gz && \
    mv apache-ant-${ANT_VERSION} /opt/ant && \
    rm apache-ant-${ANT_VERSION}-bin.tar.gz
ENV ANT_HOME /opt/ant

ENV PATH ${PATH}:/opt/ant/bin

