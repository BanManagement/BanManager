package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

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

  public PlayerNoteData(PlayerData player, PlayerData actor, String message, long created) {
    this(player, actor, message);

    this.created = created;
  }

  public boolean equalsNote(PlayerNoteData note) {
    return note.getMessage().equals(this.message)
            && note.getCreated() == this.created
            && note.getPlayer().getUUID().equals(this.getPlayer().getUUID())
            && note.getActor().getUUID().equals(this.actor.getUUID());
  }
}
