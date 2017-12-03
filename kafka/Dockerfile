FROM java:openjdk-8u91-jdk
RUN wget -q -O - https://archive.apache.org/dist/kafka/0.10.0.1/kafka_2.10-0.10.0.1.tgz |  tar -xzf - -C /usr/local
WORKDIR /usr/local/kafka_2.10-0.10.0.1
EXPOSE 9092
VOLUME /usr/local/kafka-config
ADD ./config/server.properties /usr/local/kafka-config/server.properties
ADD run-kafka.sh /usr/local/kafka_2.10-0.10.0.1/run-kafka.sh
CMD ./run-kafka.sh
