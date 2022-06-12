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
  @DatabaseField(index = true, persisterClass = IpAddress.class, canBeNull = false, columnDefinition = "VARBINARY(16) NOT NULL")
  private IPAddress ip;

  // Should always be database time
  @Getter
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL NOT NULL")
  private long join = System.currentTimeMillis() / 1000L;
  @Getter
  @Setter
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL NOT NULL")
  private long leave = System.currentTimeMillis() / 1000L;

  PlayerHistoryData() {

  }

  public PlayerHistoryData(PlayerData player) {
    this.player = player;
    this.ip = player.getIp();
  }
}
