package io.eventuate.sql.dialect;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SqlDialectConfiguration.class,
        properties= {"spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver"})
public class MsSqlDialectTest extends AbstractDialectTest {
  public MsSqlDialectTest() {
    super(MsSqlDialect.class, "(SELECT DATEDIFF_BIG(ms, '1970-01-01', GETUTCDATE()))");
  }

  @Test
  public void shouldReplaceSelectRegardlessOfCase() {
    assertEquals("select top (:limit) * FROM FOO", getDialect().addLimitToSql("sELeCt * FROM FOO", ":limit"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void shouldFailIfNotReplace() {
    getDialect().addLimitToSql("UPDATE FOO ...", ":limit");
  }
}
