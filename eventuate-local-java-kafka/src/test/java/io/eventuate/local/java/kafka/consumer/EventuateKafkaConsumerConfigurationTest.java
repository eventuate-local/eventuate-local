package io.eventuate.local.java.kafka.consumer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventuateKafkaConsumerConfigurationTest.Config.class)
public class EventuateKafkaConsumerConfigurationTest {

  @EnableConfigurationProperties(EventuateKafkaConsumerConfigurationProperties.class)
  public static class Config {
  }

  @Autowired
  private EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;

  @Test
  public void testPropertyParsing() {

    Assert.assertEquals(2, eventuateKafkaConsumerConfigurationProperties.getProperties().size());

    Assert.assertEquals("10000", eventuateKafkaConsumerConfigurationProperties.getProperties().get("session.timeout.ms"));

    Assert.assertEquals("org.apache.kafka.common.serialization.StringSerializer",
            eventuateKafkaConsumerConfigurationProperties.getProperties().get("key.serializer"));
  }
}
