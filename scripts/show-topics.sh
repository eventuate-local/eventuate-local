docker exec -it eventuatelocal_kafka_1 /bin/bash
docker exec -it eventuatelocal_kafka_1 bin/kafka-topics.sh --zookeeper zookeeper:2181 --list
docker exec -it eventuatelocal_kafka_1 bin/kafka-console-consumer.sh --zookeeper zookeeper:2181 --from-beginning --topic mysql-server-1.eventuate.events

docker exec -it eventuatelocal_kafka_1 bin/kafka-run-class.sh kafka.tools.ConsumerOffsetChecker --zookeeper zookeeper:2181 --topic io.eventuate.example.banking.domain.Account   --group javaIntegrationTestQuerySideAccountEventHandlers
docker exec -it eventuatelocal_kafka_1 bin/kafka-run-class.sh kafka.tools.ConsumerOffsetChecker --zookeeper zookeeper:2181 --topic io.eventuate.example.banking.domain.MoneyTransfer   --group javaIntegrationTestCommandSideAccountEventHandlers

docker exec -it eventuatelocal_kafka_1 bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --new-consumer --describe --group javaIntegrationTestCommandSideMoneyTransferEventHandlers
