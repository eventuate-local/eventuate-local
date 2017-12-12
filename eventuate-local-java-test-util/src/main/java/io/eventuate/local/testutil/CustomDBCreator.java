package io.eventuate.local.testutil;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomDBCreator {

  private String dataFile;
  private String dataSourceURL;
  private String driverClassName;
  private String rootUserName;
  private String rootUserPassword;
  private DataSource dataSource;
  private JdbcTemplate jdbcTemplate;

  public CustomDBCreator(String dataFile, String dataSourceURL, String driverClassName, String rootUserName, String rootUserPassword) {
    this.dataFile = dataFile;
    this.dataSourceURL = dataSourceURL;
    this.driverClassName = driverClassName;
    this.rootUserName = rootUserName;
    this.rootUserPassword = rootUserPassword;
  }

  public void create() {
    create(Optional.empty());
  }

  public void create(SqlScriptEditor sqlScriptEditor) {
    create(Optional.of(sqlScriptEditor));
  }

  public void create(Optional<SqlScriptEditor> editor) {
    dataSource = new DataSourceBuilderAdapter()
            .url(dataSourceURL)
            .driverClassName(driverClassName)
            .username(rootUserName)
            .password(rootUserPassword)
            .build();


    jdbcTemplate = new JdbcTemplate(dataSource);

    List<String> sqlList = loadSqlScriptAsListOfLines(dataFile);

    executeSql(editor.map(sqlScriptEditor -> sqlScriptEditor.edit(sqlList)).orElse(sqlList));
  }


  public List<String> loadSqlScriptAsListOfLines(String script) {
    try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(script)))) {
      return Arrays.asList(bufferedReader.lines().collect(Collectors.joining("\n")).split(";"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void executeSql(List<String> sqlList) {
    sqlList.forEach(jdbcTemplate::execute);
  }
}
