package me.confuser.banmanager.common.storage.migration;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MigrationRunnerTest {

  @Test
  public void splitStatements_singleStatement() {
    List<String> result = MigrationRunner.splitStatements("ALTER TABLE foo ADD COLUMN bar INT;");
    assertEquals(1, result.size());
    assertEquals("ALTER TABLE foo ADD COLUMN bar INT", result.get(0));
  }

  @Test
  public void splitStatements_multipleStatements() {
    String sql = "ALTER TABLE foo ADD COLUMN bar INT;\nALTER TABLE baz DROP COLUMN qux;";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(2, result.size());
    assertEquals("ALTER TABLE foo ADD COLUMN bar INT", result.get(0));
    assertEquals("ALTER TABLE baz DROP COLUMN qux", result.get(1));
  }

  @Test
  public void splitStatements_ignoreSemicolonInSingleQuotes() {
    String sql = "UPDATE foo SET bar = 'hello;world';";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(1, result.size());
    assertEquals("UPDATE foo SET bar = 'hello;world'", result.get(0));
  }

  @Test
  public void splitStatements_ignoreSemicolonInDoubleQuotes() {
    String sql = "UPDATE foo SET bar = \"hello;world\";";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(1, result.size());
    assertEquals("UPDATE foo SET bar = \"hello;world\"", result.get(0));
  }

  @Test
  public void splitStatements_skipLineComments() {
    String sql = "-- This is a comment\nALTER TABLE foo ADD COLUMN bar INT;";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(1, result.size());
    assertEquals("ALTER TABLE foo ADD COLUMN bar INT", result.get(0));
  }

  @Test
  public void splitStatements_skipBlockComments() {
    String sql = "/* block comment */ALTER TABLE foo ADD COLUMN bar INT;";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(1, result.size());
    assertEquals("ALTER TABLE foo ADD COLUMN bar INT", result.get(0));
  }

  @Test
  public void splitStatements_emptyInput() {
    List<String> result = MigrationRunner.splitStatements("");
    assertTrue(result.isEmpty());
  }

  @Test
  public void splitStatements_onlyComments() {
    String sql = "-- just a comment\n/* another comment */";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertTrue(result.isEmpty());
  }

  @Test
  public void splitStatements_noTrailingSemicolon() {
    String sql = "ALTER TABLE foo ADD COLUMN bar INT";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(1, result.size());
    assertEquals("ALTER TABLE foo ADD COLUMN bar INT", result.get(0));
  }

  @Test
  public void splitStatements_blankLinesBetween() {
    String sql = "ALTER TABLE a ADD COLUMN b INT;\n\n\nALTER TABLE c ADD COLUMN d INT;";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(2, result.size());
  }

  @Test
  public void splitStatements_multiLineStatement() {
    String sql = "ALTER TABLE foo\n  CHANGE `created` `created` BIGINT UNSIGNED,\n  CHANGE `updated` `updated` BIGINT UNSIGNED;";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(1, result.size());
    assertTrue(result.get(0).contains("CHANGE `created`"));
    assertTrue(result.get(0).contains("CHANGE `updated`"));
  }

  @Test
  public void splitStatements_escapedSingleQuote() {
    String sql = "UPDATE foo SET bar = 'O\\'Brien';";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(1, result.size());
    assertEquals("UPDATE foo SET bar = 'O\\'Brien'", result.get(0));
  }

  @Test
  public void splitStatements_escapedDoubleQuote() {
    String sql = "UPDATE foo SET bar = \"say \\\"hello\\\"\";";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(1, result.size());
    assertEquals("UPDATE foo SET bar = \"say \\\"hello\\\"\"", result.get(0));
  }

  @Test
  public void splitStatements_escapedSemicolonInQuotes() {
    String sql = "UPDATE foo SET bar = 'test\\;value'; ALTER TABLE baz ADD x INT;";
    List<String> result = MigrationRunner.splitStatements(sql);
    assertEquals(2, result.size());
    assertEquals("UPDATE foo SET bar = 'test\\;value'", result.get(0));
    assertEquals("ALTER TABLE baz ADD x INT", result.get(1));
  }
}
