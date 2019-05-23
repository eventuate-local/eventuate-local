package io.eventuate.sql.dialect;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SqlDialectConfiguration.class,
        properties= {"spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver"})
public class MsSqlDialectTest extends AbstractDialectTest {
  public MsSqlDialectTest() {
    super(MsSqlDialect.class, "(SELECT DATEDIFF_BIG(ms, '1970-01-01', GETUTCDATE()))");
  }
}
