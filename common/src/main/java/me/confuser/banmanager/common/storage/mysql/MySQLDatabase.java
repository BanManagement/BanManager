package me.confuser.banmanager.common.storage.mysql;

import me.confuser.banmanager.common.ormlite.db.MysqlDatabaseType;

public class MySQLDatabase extends MysqlDatabaseType {

  private final static String DRIVER_CLASS_NAME = "me.confuser.banmanager.common.mysql.cj.jdbc.Driver";

  public MySQLDatabase() {
    setCreateTableSuffix("ENGINE=InnoDB DEFAULT CHARSET=utf8");
  }

  @Override
  protected String getDriverClassName() {
    return DRIVER_CLASS_NAME;
  }
}
