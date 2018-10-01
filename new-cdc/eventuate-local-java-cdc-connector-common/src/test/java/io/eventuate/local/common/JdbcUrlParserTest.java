package io.eventuate.local.common;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JdbcUrlParserTest {

  @Test
  public void shouldParseUrl() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://192.168.99.101/eventuate");
    assertEquals("192.168.99.101", jdbcUrl.getHost());
    assertEquals(3306, jdbcUrl.getPort());
    assertEquals("eventuate", jdbcUrl.getDatabase());
    assertEquals("mysql", jdbcUrl.getDbms());
    assertTrue(jdbcUrl.isMySql());
  }

  @Test
  public void shouldParsePostgresUrl() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:postgresql://192.168.99.101/eventuate");
    assertEquals("192.168.99.101", jdbcUrl.getHost());
    assertEquals(5432, jdbcUrl.getPort());
    assertEquals("eventuate", jdbcUrl.getDatabase());
    assertEquals("postgresql", jdbcUrl.getDbms());
    assertTrue(jdbcUrl.isPostgres());
  }

  @Test
  public void shouldParseUrlWithPort() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://192.168.99.101:3306/eventuate");
    assertEquals("192.168.99.101", jdbcUrl.getHost());
    assertEquals(3306, jdbcUrl.getPort());
    assertEquals("eventuate", jdbcUrl.getDatabase());
    assertEquals("mysql", jdbcUrl.getDbms());
    assertTrue(jdbcUrl.isMySql());
  }

  @Test
  public void shouldParseUrlWithParameters() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://192.168.99.101:3306/eventuate?useUnicode=true");
    assertEquals("192.168.99.101", jdbcUrl.getHost());
    assertEquals(3306, jdbcUrl.getPort());
    assertEquals("eventuate", jdbcUrl.getDatabase());
    assertEquals("mysql", jdbcUrl.getDbms());
    assertTrue(jdbcUrl.isMySql());
  }

}