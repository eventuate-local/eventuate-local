package io.eventuate.local.testutil;

import java.util.List;

public class EventuateLocalCustomDBSqlEditor implements SqlScriptEditor {

  @Override
  public List<String> edit(List<String> sqlList) {
    sqlList.set(0, sqlList.get(0).replace("create database", "create database if not exists"));
    for (int i = 0; i < 3; i++) sqlList.set(i, sqlList.get(i).replace("eventuate", "custom"));
    return sqlList;
  }
}
