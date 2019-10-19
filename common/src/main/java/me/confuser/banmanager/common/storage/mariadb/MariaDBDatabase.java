package me.confuser.banmanager.common.storage.mariadb;

import com.j256.ormlite.db.MariaDbDatabaseType;

public class MariaDBDatabase extends MariaDbDatabaseType {

  public MariaDBDatabase() {
    setCreateTableSuffix("ENGINE=InnoDB DEFAULT CHARSET=utf8");
  }
}
