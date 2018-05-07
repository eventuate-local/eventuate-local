package io.eventuate.local.java.kafka.producer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventuateKafkaProducerConfigurationTest.Config.class)
public class EventuateKafkaProducerConfigurationTest {

  @EnableConfigurationProperties(EventuateKafkaProducerConfigurationProperties.class)
  public static class Config {
  }

  @Autowired
  private EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties;

  @Test
  public void testPropertyParsing() {

    Assert.assertEquals(2, eventuateKafkaProducerConfigurationProperties.getProperties().size());

    Assert.assertEquals("1000000", eventuateKafkaProducerConfigurationProperties.getProperties().get("buffer.memory"));

    Assert.assertEquals("org.apache.kafka.common.serialization.StringSerializer",
            eventuateKafkaProducerConfigurationProperties.getProperties().get("value.serializer"));
  }
}
