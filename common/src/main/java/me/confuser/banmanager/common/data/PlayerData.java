package me.confuser.banmanager.common.data;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.ipaddr.AddressStringException;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.PlayerStorage;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.storage.mysql.IpAddress;
import me.confuser.banmanager.common.util.UUIDUtils;

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
  @DatabaseField(index = true, persisterClass = IpAddress.class, columnDefinition = "VARBINARY(16) NOT NULL")
  private IPAddress ip;
  @Getter
  @DatabaseField(columnDefinition = "BIGINT UNSIGNED NOT NULL")
  private long lastSeen = System.currentTimeMillis() / 1000L;

  private UUID uuid = null;

  PlayerData() {

  }

  public PlayerData(UUID uuid, String name) {
    this.uuid = uuid;
    this.id = UUIDUtils.toBytes(uuid);
    this.name = name;

    try {
      this.ip = new IPAddressString("127.0.0.1").toAddress();
    } catch (AddressStringException e) {
      e.printStackTrace();
    }

    this.lastSeen = System.currentTimeMillis() / 1000L;
  }

  public PlayerData(UUID uuid, String name, IPAddress ip) {
    this.uuid = uuid;
    this.id = UUIDUtils.toBytes(uuid);
    this.name = name;
    this.ip = ip;
    this.lastSeen = System.currentTimeMillis() / 1000L;
  }

  public PlayerData(UUID uuid, String name, IPAddress ip, long lastSeen) {
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
