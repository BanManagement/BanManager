package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

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

  @DatabaseField(canBeNull = false, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long expired;

  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;

  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData pastActor;

  @DatabaseField(canBeNull = false, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long pastCreated;

  @DatabaseField(index = true, canBeNull = false, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  @DatabaseField(canBeNull = false)
  @Getter
  private String createdReason;

  @DatabaseField(index = true)
  @Getter
  private boolean soft = false;

  @DatabaseField
  @Getter
  private boolean silent = false;

  @DatabaseField
  @Getter
  private boolean onlineOnly = false;

  @DatabaseField(columnDefinition = "BIGINT UNSIGNED NOT NULL DEFAULT 0")
  @Getter
  private long remainingOnlineTime = 0;

  PlayerMuteRecord() {

  }

  public PlayerMuteRecord(PlayerMuteData mute, PlayerData actor, String reason) {
    player = mute.getPlayer();
    expired = mute.getExpires();
    pastActor = mute.getActor();
    pastCreated = mute.getCreated();
    createdReason = reason;
    silent = mute.isSilent();
    soft = mute.isSoft();
    onlineOnly = mute.isOnlineOnly();

    if (mute.isOnlineOnly()) {
      if (mute.isPaused()) {
        remainingOnlineTime = mute.getPausedRemaining();
      } else if (mute.getExpires() > 0) {
        long now = System.currentTimeMillis() / 1000L;
        remainingOnlineTime = Math.max(0, mute.getExpires() - now);
      }
    }

    this.reason = mute.getReason();
    this.actor = actor;
  }

  public PlayerMuteRecord(PlayerMuteData mute, PlayerData actor, long created) {
    player = mute.getPlayer();
    reason = mute.getReason();
    expired = mute.getExpires();
    pastActor = mute.getActor();
    pastCreated = mute.getCreated();
    silent = mute.isSilent();
    soft = mute.isSoft();
    onlineOnly = mute.isOnlineOnly();

    if (mute.isOnlineOnly()) {
      if (mute.isPaused()) {
        remainingOnlineTime = mute.getPausedRemaining();
      } else if (mute.getExpires() > 0) {
        long now = System.currentTimeMillis() / 1000L;
        remainingOnlineTime = Math.max(0, mute.getExpires() - now);
      }
    }

    this.actor = actor;
    this.created = created;
  }

  public boolean equalsMute(PlayerMuteData mute) {
    return mute.getReason().equals(this.reason)
            && mute.getExpires() == expired
            && mute.getCreated() == this.pastCreated
            && mute.getPlayer().getUUID().equals(this.getPlayer().getUUID())
            && mute.getActor().getUUID().equals(this.pastActor.getUUID())
            && mute.isSoft() == soft;
  }
}
