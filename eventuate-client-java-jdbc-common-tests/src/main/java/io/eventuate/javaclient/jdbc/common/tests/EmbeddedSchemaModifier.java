package io.eventuate.javaclient.jdbc.common.tests;

import io.eventuate.common.jdbc.EventuateSchema;

import java.util.List;
import java.util.function.Function;

public class EmbeddedSchemaModifier {

  private EventuateSchema eventuateSchema;
  private String embeddedSchema;

  public EmbeddedSchemaModifier(EventuateSchema eventuateSchema, String embeddedSchema) {
    this.eventuateSchema = eventuateSchema;
    this.embeddedSchema = embeddedSchema;
  }

  public List<String> getModifiedSqlLines(Function<String, List<String>> sqlLoader) {
    List<String> lines = sqlLoader.apply(embeddedSchema);

    if (!eventuateSchema.isEmpty()) {
      for (int i = 0; i < 2; i++) lines.set(i, lines.get(i).replace("eventuate", eventuateSchema.getEventuateDatabaseSchema()));
    } else {
      lines = lines.subList(2, lines.size());
    }

    return lines;
  }
}
