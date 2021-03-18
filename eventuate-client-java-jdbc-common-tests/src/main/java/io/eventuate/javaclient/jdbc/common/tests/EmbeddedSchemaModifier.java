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

    for (int i = 0; i < lines.size(); i++){
      replaceLine(lines, i, "eventuate\\.", eventuateSchema.isEmpty() ? "" : eventuateSchema.getEventuateDatabaseSchema() + ".");
      if (!eventuateSchema.isEmpty()) {
        replaceLine(lines, i, "eventuate", eventuateSchema.getEventuateDatabaseSchema());
      }
    }

    return lines;
  }

  private void replaceLine(List<String> lines, int line, String target, String replacement) {
    lines.set(line, lines.get(line).replaceAll(target, replacement));
  }
}
