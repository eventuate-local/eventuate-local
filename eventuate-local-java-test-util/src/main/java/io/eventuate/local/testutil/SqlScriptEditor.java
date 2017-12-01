package io.eventuate.local.testutil;

import java.util.List;

public interface SqlScriptEditor {
  List<String> edit(List<String> sqlList);
}
