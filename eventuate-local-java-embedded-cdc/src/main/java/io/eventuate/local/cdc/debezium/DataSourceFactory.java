package io.eventuate.local.cdc.debezium;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

public class DataSourceFactory {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${spring.datasource.url}")
  private String datasourceUrl;

  @Value("${spring.datasource.username}")
  private String datasourceUsername;

  @Value("${spring.datasource.password}")
  private String datasourcePassword;

  @Value("${spring.datasource.driver-class-name}")
  private String datasourceDriverClassName;

  private Optional<DataSource> dataSource = Optional.empty();

  public javax.sql.DataSource createDataSource() {
	this.dataSource.ifPresent(ds -> ds.close(true));

    DataSource dataSource = new DataSource();

    dataSource.setDriverClassName(datasourceDriverClassName);
    dataSource.setUrl(datasourceUrl);
    dataSource.setUsername(datasourceUsername);
    dataSource.setPassword(datasourcePassword);

    this.dataSource = Optional.of(dataSource);

    return dataSource;
  }
}
