package me.confuser.banmanager.storage.mysql;

import com.j256.ormlite.db.MysqlDatabaseType;

public class MySQLDatabase extends MysqlDatabaseType {

  private final static String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

  public MySQLDatabase() {
    setCreateTableSuffix("ENGINE=InnoDB DEFAULT CHARSET=utf8");
  }

  @Override
  protected String getDriverClassName() {
    return DRIVER_CLASS_NAME;
  }
}
