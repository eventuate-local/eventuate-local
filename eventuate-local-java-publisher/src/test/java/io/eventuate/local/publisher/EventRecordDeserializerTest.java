package io.eventuate.local.publisher;

import io.eventuate.local.publisher.changes.EventRecord;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class EventRecordDeserializerTest {

  @Test
  public void shouldDeserialize() throws IOException {
    InputStream is = getClass().getResourceAsStream("/example-event.json");
    EventRecord er = EventRecordDeserializer.toEventRecord(IOUtils.toString(is));
    assertEquals("000001561eae7c09-0000000000000007", er.getPayload().getAfter().getEvent_id());
  }

}