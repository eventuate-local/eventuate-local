package io.eventuate.local.testutil;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

public class CustomDBCreator {

  private String dataFile;
  private String dataSourceURL;
  private String driverClassName;
  private String rootUserName;
  private String rootUserPassword;

  public CustomDBCreator(String dataFile, String dataSourceURL, String driverClassName, String rootUserName, String rootUserPassword) {
    this.dataFile = dataFile;
    this.dataSourceURL = dataSourceURL;
    this.driverClassName = driverClassName;
    this.rootUserName = rootUserName;
    this.rootUserPassword = rootUserPassword;
  }

  public void create() {
    DataSource dataSource = DataSourceBuilder
            .create()
            .url(dataSourceURL)
            .driverClassName(driverClassName)
            .username(rootUserName)
            .password(rootUserPassword)
            .build();

    Resource resource = new ClassPathResource(dataFile);
    ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
    databasePopulator.execute(dataSource);
  }
}
