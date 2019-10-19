package me.confuser.banmanager.common.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerKickData {

  @Getter
  @DatabaseField(generatedId = true)
  private int id;
  @Getter
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData player;
  @Getter
  @DatabaseField(canBeNull = false)
  private String reason;
  @Getter
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData actor;
  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  PlayerKickData() {

  }

  public PlayerKickData(PlayerData player, PlayerData actor, String reason) {
    this.player = player;
    this.reason = reason;
    this.actor = actor;
  }

  // Imports only!
  public PlayerKickData(PlayerData player, PlayerData actor, String reason, long created) {
    this.player = player;
    this.reason = reason;
    this.actor = actor;
    this.created = created;
  }
}
