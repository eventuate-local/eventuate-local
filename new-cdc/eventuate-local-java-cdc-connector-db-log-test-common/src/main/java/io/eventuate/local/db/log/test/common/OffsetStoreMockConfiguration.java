package io.eventuate.local.db.log.test.common;

import io.eventuate.local.db.log.common.OffsetStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OffsetStoreMockConfiguration {

  @Bean
  @Primary
  public OffsetStore offsetStore() {
    return new OffsetStoreMock();
  }

}
