package me.confuser.banmanager.common.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.storage.PlayerStorage;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.net.InetAddress;
import java.util.UUID;

@DatabaseTable(tableName = "players", daoClass = PlayerStorage.class)
public class PlayerData {

  @DatabaseField(id = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private byte[] id;
  @DatabaseField(index = true, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  @Setter
  private String name;
  @Getter
  @DatabaseField(index = true, columnDefinition = "INT UNSIGNED NOT NULL")
  private long ip;
  @Getter
  @DatabaseField(columnDefinition = "INT(10) NOT NULL")
  private long lastSeen = System.currentTimeMillis() / 1000L;

  private UUID uuid = null;

  PlayerData() {

  }

  public PlayerData(UUID uuid, String name) {
    this.uuid = uuid;
    this.id = UUIDUtils.toBytes(uuid);
    this.name = name;
    this.ip = IPUtils.toLong("127.0.0.1");
    this.lastSeen = System.currentTimeMillis() / 1000L;
  }

  public PlayerData(UUID uuid, String name, InetAddress ip) {
    this.uuid = uuid;
    this.id = UUIDUtils.toBytes(uuid);
    this.name = name;
    this.ip = IPUtils.toLong(ip);
    this.lastSeen = System.currentTimeMillis() / 1000L;
  }

  public PlayerData(UUID uuid, String name, long ip, long lastSeen) {
    this.uuid = uuid;
    this.id = UUIDUtils.toBytes(uuid);
    this.name = name;
    this.ip = ip;
    this.lastSeen = lastSeen;
  }

  public UUID getUUID() {
    if (uuid == null) {
      uuid = UUIDUtils.fromBytes(id);
    }

    return uuid;
  }
}
