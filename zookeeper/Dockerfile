FROM java:openjdk-8u91-jdk

RUN (wget -q -O - https://archive.apache.org/dist/zookeeper/zookeeper-3.4.8/zookeeper-3.4.8.tar.gz | tar -xzf - -C /usr/local) && \
  sed -e "s?/tmp/zookeeper?/usr/local/zookeeper-data?" < /usr/local/zookeeper-3.4.8/conf/zoo_sample.cfg > /usr/local/zookeeper-3.4.8/conf/zoo.cfg && \
  mkdir -p /usr/local/zookeeper-data && \
  mkdir -p /usr/local/zookeeper-conf

EXPOSE 2181 2888 3888

WORKDIR /usr/local/zookeeper-3.4.8

VOLUME ["/usr/local/zookeeper-3.4.8/conf", "/usr/local/zookeeper-data", "/usr/local/zookeeper-conf"]

ENTRYPOINT ["/usr/local/zookeeper-3.4.8/bin/zkServer.sh"]
CMD ["start-foreground"]
