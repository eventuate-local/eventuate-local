package io.eventuate.local.unified.cdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.local.unified.cdc.properties.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class PropertyValidationTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testMySqlBinlogProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();

    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class, "type must not be null");

    propertyBuilder.addString("type", CdcPipelineType.MYSQL_BINLOG.stringRepresentation);

    testCommonRequiredProperties(MySqlBinlogCdcPipelineProperties.class, propertyBuilder);

    propertyBuilder.addString("cdcDbUserName", "rootUser");
    assertExceptionMessage(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class, "cdcDbPassword must not be null");

    propertyBuilder.addString("cdcDbPassword", "rootUser");
    assertNoException(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class);

    assertNoException(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class);

    MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties = objectMapper.readValue(propertyBuilder.toString(), MySqlBinlogCdcPipelineProperties.class);

    testCommonDbLogDefaultProperties(mySqlBinlogCdcPipelineProperties);

    Assert.assertNotNull(mySqlBinlogCdcPipelineProperties.getBinlogClientId());
    Assert.assertEquals("eventuate.local.cdc.my-sql-connector.offset.storage", mySqlBinlogCdcPipelineProperties.getOldDbHistoryTopicName());
  }

  @Test
  public void testPollingProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();
    assertExceptionMessage(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class, "type must not be null");

    propertyBuilder.addString("type", CdcPipelineType.EVENT_POLLING.stringRepresentation);
    testCommonRequiredProperties(PostgresWalCdcPipelineProperties.class, propertyBuilder);

    assertNoException(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class);

    PollingPipelineProperties pollingPipelineProperties = objectMapper.readValue(propertyBuilder.toString(), PollingPipelineProperties.class);

    Assert.assertEquals(500, (int)pollingPipelineProperties.getPollingIntervalInMilliseconds());
    Assert.assertEquals(1000, (int)pollingPipelineProperties.getMaxEventsPerPolling());
    Assert.assertEquals(100, (int)pollingPipelineProperties.getMaxAttemptsForPolling());
    Assert.assertEquals(500, (int)pollingPipelineProperties.getPollingRetryIntervalInMilliseconds());
  }

  @Test
  public void testPostgresWalProperties() throws Exception {
    PropertyBuilder propertyBuilder = new PropertyBuilder();
    assertExceptionMessage(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class, "type must not be null");

    propertyBuilder.addString("type", CdcPipelineType.POSTGRES_WAL.stringRepresentation);
    testCommonRequiredProperties(PostgresWalCdcPipelineProperties.class, propertyBuilder);

    assertNoException(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class);

    PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties = objectMapper.readValue(propertyBuilder.toString(), PostgresWalCdcPipelineProperties.class);

    testCommonDbLogDefaultProperties(postgresWalCdcPipelineProperties);
  }

  private void testCommonDbLogDefaultProperties(CommonDbLogCdcPipelineProperties commonDbLogCdcPipelineProperties) {
    Assert.assertEquals("db.history.topic", commonDbLogCdcPipelineProperties.getDbHistoryTopicName());
    Assert.assertEquals("MySqlBinLog", commonDbLogCdcPipelineProperties.getMySqlBinLogClientName());
    Assert.assertEquals(5000, (int)commonDbLogCdcPipelineProperties.getBinlogConnectionTimeoutInMilliseconds());
    Assert.assertEquals(100, (int)commonDbLogCdcPipelineProperties.getMaxAttemptsForBinlogConnection());
  }

  private <PROPERTIES extends CdcPipelineProperties> void testCommonRequiredProperties(Class<PROPERTIES> propertyClass,
                                                                                       PropertyBuilder propertyBuilder) throws Exception {
    propertyBuilder.addString("dataSourceUrl", "jdbc:correctdb://localhost/eventuate");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourceUserName must not be null");

    propertyBuilder.addString("dataSourceUserName", "testUser");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourcePassword must not be null");

    propertyBuilder.addString("dataSourcePassword", "testPassword");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "dataSourceDriverClassName must not be null");

    propertyBuilder.addString("dataSourceDriverClassName", "com.correct.db.Driver");
    assertExceptionMessage(propertyBuilder.toString(), propertyClass, "leadershipLockPath must not be null");

    propertyBuilder.addString("leadershipLockPath", "/eventuate/leader/test");
  }

  private <PROPERTIES extends CdcPipelineProperties> void assertExceptionMessage(String properties,
                                                                                 Class<PROPERTIES> propertyClass,
                                                                                 String message) throws Exception {
    assertExceptionMessage(properties, propertyClass, Optional.of(message));
  }

  private <PROPERTIES extends CdcPipelineProperties> void assertNoException(String properties,
                                                                            Class<PROPERTIES> propertyClass) throws Exception {
    assertExceptionMessage(properties, propertyClass, Optional.empty());
  }

  private <PROPERTIES extends CdcPipelineProperties> void assertExceptionMessage(String properties,
                                                                                 Class<PROPERTIES> propertyClass,
                                                                                 Optional<String> message) throws Exception {

    PROPERTIES cdcPipelineProperties = objectMapper.readValue(properties, propertyClass);

    Exception exception = null;

    try {
      cdcPipelineProperties.validate();
    } catch (IllegalArgumentException e) {
      exception = e;
    }

    Exception ee = exception;

    message.map(msg -> {
      Assert.assertNotNull(ee);
      Assert.assertEquals(msg, ee.getMessage());
      return msg;
    }).orElseGet(() -> {
      Assert.assertNull(ee);
      return null;
    });
  }

  private static class PropertyBuilder {
    private List<Entry> entries = new ArrayList<>();

    public void addString(String key, String value) {
      entries.add(new Entry(key, String.format("\"%s\"", value)));
    }

    @Override
    public String toString() {
      StringJoiner stringJoiner = new StringJoiner(", ");
      entries.forEach(e -> stringJoiner.add(e.toString()));
      return String.format("{%s}", stringJoiner.toString());
    }

    private static class Entry {
      final String key;
      final String value;

      public Entry(String key, String value) {
        this.key = key;
        this.value = value;
      }

      @Override
      public String toString() {
        return String.format("\"%s\" : %s", key, value);
      }
    }
  }
}
