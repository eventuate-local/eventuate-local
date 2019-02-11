package io.eventuate.local.mysql.binlog;

import io.eventuate.local.common.OffsetStoreCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OffsetStoreMockConfiguration {

  private static final OffsetStoreMock offsetStoreMock = new OffsetStoreMock();

  @Bean
  @Primary
  public OffsetStoreCreator offsetStoreCreator() {
    return dataProducer -> offsetStoreMock;
  }

}
