package me.confuser.banmanager.common.data;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.storage.mysql.IpAddress;

@DatabaseTable
public class PlayerHistoryData {

  @Getter
  @DatabaseField(generatedId = true)
  private int id;

  @Getter
  @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData player;

  @Getter
  @DatabaseField(index = true, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  private String name;

  @Getter
  @DatabaseField(index = true, persisterClass = IpAddress.class, canBeNull = true, columnDefinition = "VARBINARY(16)")
  private IPAddress ip;

  @Getter
  @Setter
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  private long join = 0L;

  @Getter
  @Setter
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  private long leave = 0L;

  PlayerHistoryData() {
  }

  /**
   * Create a session history record.
   * Join time is set to the current time when the session is created.
   *
   * @param player The player data
   * @param logIp Whether to record the IP address (false = ip will be null)
   */
  public PlayerHistoryData(PlayerData player, boolean logIp) {
    this.player = player;
    this.name = player.getName();
    this.ip = logIp ? player.getIp() : null;
    this.join = System.currentTimeMillis() / 1000L;
  }
}
