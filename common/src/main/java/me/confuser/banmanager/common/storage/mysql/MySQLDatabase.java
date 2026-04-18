package me.confuser.banmanager.common.storage.mysql;

import me.confuser.banmanager.common.ormlite.jdbc.db.MysqlDatabaseType;

public class MySQLDatabase extends MysqlDatabaseType {

  private final static String DRIVER_CLASS_NAME = "me.confuser.banmanager.common.mysql.cj.jdbc.Driver";

  public MySQLDatabase() {
    setCreateTableSuffix("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
  }

  @Override
  protected String[] getDriverClassNames() {
    return new String[] { DRIVER_CLASS_NAME };
  }
}
