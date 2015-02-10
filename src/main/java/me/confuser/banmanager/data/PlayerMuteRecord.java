package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerMuteRecord {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;
  @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;
  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;
  @DatabaseField(canBeNull = false)
  @Getter
  private long expired;
  @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;
  @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData pastActor;
  @DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long pastCreated;
  @DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  PlayerMuteRecord() {

  }

  public PlayerMuteRecord(PlayerMuteData mute, PlayerData actor) {
    player = mute.getPlayer();
    reason = mute.getReason();
    expired = mute.getExpires();
    pastActor = mute.getActor();
    pastCreated = mute.getCreated();

    this.actor = actor;
  }

  public PlayerMuteRecord(PlayerMuteData mute, PlayerData actor, long created) {
    player = mute.getPlayer();
    reason = mute.getReason();
    expired = mute.getExpires();
    pastActor = mute.getActor();
    pastCreated = mute.getCreated();

    this.actor = actor;
    this.created = created;
  }

  public boolean equalsMute(PlayerMuteData mute) {
    return mute.getReason().equals(this.reason)
            && mute.getExpires() == expired
            && mute.getCreated() == this.pastCreated
            && mute.getPlayer().getUUID().equals(this.getPlayer().getUUID())
            && mute.getActor().getUUID().equals(this.pastActor.getUUID());
  }
}
