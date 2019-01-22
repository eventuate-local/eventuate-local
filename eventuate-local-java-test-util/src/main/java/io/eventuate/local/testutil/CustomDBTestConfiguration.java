package io.eventuate.local.testutil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:/customdb.properties"})
@EnableAutoConfiguration
public class CustomDBTestConfiguration {

  @Value("${data.file}")
  private String dataFile;

  @Value("${spring.datasource.url}")
  private String dataSourceURL;

  @Value("${spring.datasource.driver.class.name}")
  private String driverClassName;

  @Value("${eventuatelocal.cdc.db.user.name}")
  private String rootUserName;

  @Value("${eventuatelocal.cdc.db.password}")
  private String rootUserPassword;

  @Bean
  public CustomDBCreator customDBCreator() {
    return new CustomDBCreator(dataFile, dataSourceURL, driverClassName, rootUserName, rootUserPassword);
  }

  @Bean
  public SqlScriptEditor eventuateLocalCustomDBSqlEditor() {
    return new EventuateLocalCustomDBSqlEditor();
  }
}
