package me.confuser.banmanager.api.database;

/**
 * Logical identifier for one of BanManager's two database connection
 * pools. Used wherever the API needs the caller to name a database
 * without exposing the underlying {@link javax.sql.DataSource} (which
 * would invite reference-equality bugs when callers pass wrapped or
 * proxied instances).
 */
public enum DatabaseKind {

  /**
   * The local database. Always present — every BanManager install has a
   * local database for player records and history.
   */
  LOCAL,

  /**
   * The optional global database. Only present when the operator has
   * configured cross-server sharing; otherwise
   * {@link DatabaseAccess#globalDataSource()} returns
   * {@link java.util.Optional#empty()}.
   */
  GLOBAL
}
