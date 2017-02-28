package io.eventuate.local.cdc.debezium;

import org.junit.Test;

import static org.junit.Assert.*;

public class JdbcUrlParserTest {

  @Test
  public void shouldParseUrl() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://192.168.99.101/eventuate");
    assertEquals("192.168.99.101", jdbcUrl.getHost());
    assertEquals(3306, jdbcUrl.getPort());
    assertEquals("eventuate", jdbcUrl.getDatabase());
  }

  @Test
  public void shouldParseUrlWithPort() {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse("jdbc:mysql://192.168.99.101:3306/eventuate");
    assertEquals("192.168.99.101", jdbcUrl.getHost());
    assertEquals(3306, jdbcUrl.getPort());
    assertEquals("eventuate", jdbcUrl.getDatabase());
  }

}