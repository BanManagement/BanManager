package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerNoteData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;
  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;
  @DatabaseField(canBeNull = false)
  @Getter
  private String message;
  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;
  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  PlayerNoteData() {

  }

  public PlayerNoteData(PlayerData player, PlayerData actor, String message) {
    this.player = player;
    this.message = message;
    this.actor = actor;
  }
}
