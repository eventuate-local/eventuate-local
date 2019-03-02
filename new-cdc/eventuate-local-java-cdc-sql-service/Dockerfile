FROM java:openjdk-8u91-jdk
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["./run.sh"]
COPY build/libs/*SNAPSHOT.jar .
COPY run.sh .
RUN chmod u+x run.sh
