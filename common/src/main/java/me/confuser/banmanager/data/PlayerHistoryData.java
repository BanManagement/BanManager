package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerHistoryData {

  @Getter
  @DatabaseField(generatedId = true)
  private int id;
  @Getter
  @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData player;
  @Getter
  @DatabaseField(index = true, canBeNull = false, columnDefinition = "INT UNSIGNED NOT NULL")
  private long ip;

  // Should always be database time
  @Getter
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  private long join = System.currentTimeMillis() / 1000L;
  @Getter
  @Setter
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  private long leave = System.currentTimeMillis() / 1000L;

  PlayerHistoryData() {

  }

  public PlayerHistoryData(PlayerData player) {
    this.player = player;
    this.ip = player.getIp();
  }
}
