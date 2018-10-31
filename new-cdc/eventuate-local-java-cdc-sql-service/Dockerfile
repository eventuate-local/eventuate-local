FROM java:openjdk-8u91-jdk
COPY build/libs/*SNAPSHOT.jar .
COPY run.sh .
RUN chmod u+x run.sh
ENTRYPOINT ["./run.sh"]
