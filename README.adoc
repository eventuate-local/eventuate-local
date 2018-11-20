= Eventuate Local

image::https://api.bintray.com/packages/eventuateio-oss/eventuate-maven-release/eventuate-local/images/download.svg[link="https://bintray.com/eventuateio-oss/eventuate-maven-release/eventuate-local/_latestVersion"]
image::http://eventuate.io/i/logo.gif[]

== Simplifying the development of transactional microservices

Eventuate&trade; Local is the on-premise, open source version of http://eventuate.io/[Eventuate&trade;], which is a platform for developing transactional business applications that use the microservice architecture.
Eventuate provides an event-driven programming model for microservices that is based on event sourcing and CQRS.
Eventuate&trade; Local has the same API as the SaaS version but uses a SQL database to persist events and Kafka as the publish/subscribe mechanism.

Eventuate Local consists of:

* A framework for developing (microservice-based) applications.
* An event store consisting of a SQL database (currently MySQL) and Kafka.

image:https://raw.githubusercontent.com/eventuate-local/eventuate-local/master/i/Eventuate%20Local%20Big%20Picture.png[Big Picture]

The framework persists events in an `EVENTS` table in the SQL database and subscribes to events in Kafka.
A change data capture component  tails the database transaction log and publishes each event to Kafka.
There is a Kafka topic for each aggregate type.

= About change data capture

Eventuate Local has a change data capture (CDC) component that

1. tails the MySQL transaction log
2. publishes each event inserted into the `EVENTS` table to Kafka topic for the event's aggregate.

The CDC component runs either embedded within each application or as a service `cdcservice`.

== Got questions?

Don't hesitate to create an issue or see

* https://groups.google.com/d/forum/eventuate-users[Mailing list]
* https://eventuate-users.slack.com[Slack]. https://eventuateusersslack.herokuapp.com/[Get invite]
* http://eventuate.io/contact.html[Contact us].

== Need support?

Take a look at the http://eventuate.io/support.html[available paid support] options.


= Setting up Eventuate Local

To use Eventuate Local you need to

1. Create `EVENT` and `ENTITIES` tables in a MySQL database.
2. Run Apache Zookeeper, Apache Kafka and the optional `cdcservice`
3. Use the Eventuate Local artifacts in your application.

The easiest way to get started is to run a set of Docker containers using Docker Compose as described below.

== The quick setup

This is the fastest way to get started with Eventuate Local.

=== Set the DOCKER_HOST_IP environment variable

You must first set the environment variable `DOCKER_HOST_IP` to the IP address of the machine running Docker.
For example, if you are running Docker Machine on Mac/Windows this would be the IP address of the VirtualBox VM.
Please note that you cannot set `DOCKER_HOST_IP` to `localhost` since that will not resolve to the correct IP address within a Docker container.

=== Run the Eventuate Local Docker containers

Next, you can run the Docker containers.
First, copy https://github.com/eventuate-local/eventuate-local/blob/master/docker-compose-eventuate-local.yml[docker-compose-eventuate-local.yml] to your project.
Then, launch the Docker containers by running the following command:

```
docker-compose -f docker-compose-eventuate-local.yml
```

This command creates the following containers:

* Apache Zookeeper - used by both the change data capture component and Kafka
* Apache Kafka - message broker
* MySQL - MySQL database that has the eventuate schema already defined
* cdcservice - the change data capture component

For convenience, you might want to add the contents of this file to your project's `docker-compose.yml` file.

=== Set some environment variables

In order for your Sprint Boot application to use Eventuate Local you need to set the following application properties:

----
spring.datasource.url=jdbc:mysql://${DOCKER_HOST_IP}/eventuate
spring.datasource.username=mysqluser
spring.datasource.password=mysqlpw
eventuateLocal.kafka.bootstrapServers=$DOCKER_HOST_IP:9092
eventuateLocal.zookeeper.connectionString=$DOCKER_HOST_IP:2181
eventuateLocal.cdc.dbUserName=root
eventuateLocal.cdc.dbPassword=rootpassword
----


A convenient way to do that is to set the corresponding OS environment variables:

----
export SPRING_DATASOURCE_URL=jdbc:mysql://${DOCKER_HOST_IP}/eventuate
export SPRING_DATASOURCE_USERNAME=mysqluser
export SPRING_DATASOURCE_PASSWORD=mysqlpw
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver
export EVENTUATELOCAL_KAFKA_BOOTSTRAP_SERVERS=$DOCKER_HOST_IP:9092
export EVENTUATELOCAL_CDC_DB_USER_NAME=root
export EVENTUATELOCAL_CDC_DB_PASSWORD=rootpassword
export EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING=$DOCKER_HOST_IP:2181
----

You can do that by running the https://github.com/eventuate-local/eventuate-local/blob/master/scripts/set-env.sh[set-env.sh] bash script:

=== Use the Eventuate Local libraries

If you are using Gradle then please specify the following in `gradle.properties`:

```
eventuateLocalVersion=0.11.0.RELEASE
```

and instead of the Eventuate HTTP/STOMP artifacts, specify the following:

```
compile "io.eventuate.local.java:eventuate-local-java-jdbc:${eventuateLocalVersion}"
compile "io.eventuate.local.java:eventuate-local-java-embedded-cdc-autoconfigure:${eventuateLocalVersion}"
```
For more information about developing applications with Eventuate Local see the http://eventuate.io/gettingstartedv2.html[Getting Started guide].

=== Configuring your application containers

You need to configure your application's containers to connect to the Eventuate MySQL, Kafka and Zookeeper containers.
You can do that using the following in your project's `docker-compose.yml` file using `links` and `environment`:

```
mycontainer:
  ...
  links:
    - mysql
    - kafka
    - zookeeper
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql/eventuate
    SPRING_DATASOURCE_USERNAME: mysqluser
    SPRING_DATASOURCE_PASSWORD: mysqlpw
    SPRING_DATASOURCE_DRIVER_CLASS_NAME: com.mysql.jdbc.Driver
    EVENTUATELOCAL_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING: zookeeper:2181
    EVENTUATELOCAL_CDC_DB_USER_NAME: root
    EVENTUATELOCAL_CDC_DB_PASSWORD: rootpassword
```

Note: in order for this to work you have either copied the container definitions from `docker-compose-eventuate-local.yml` to you `docker-compose.yml` file or you are running `docker-compose` with multiple `-f` arguments:

```
docker-compose -f docker-compose-eventuate-local.yml -f docker-compose.yml up -d
```

== The not so quick version

TBD

= Running an example application

The http://eventuate.io/exampleapps.html[Eventuate example applications] support both Eventuate and Eventuate Local.

To build an example with Eventuate Local, use this command:

```
./gradlew -P eventuateDriver=local assemble
```

To start the Docker Containers with Eventuate Local run this command:

```
docker-compose -f docker-compose-eventuate-local.yml up -d
```

The `docker-compose-eventuate-local.yml` file defines the application containers and the Eventuate Local containers and links them appropriately.
