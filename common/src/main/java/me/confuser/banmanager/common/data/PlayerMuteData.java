package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerMuteData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;
  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long updated = System.currentTimeMillis() / 1000L;
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long expires = 0;


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
  private long pausedRemaining = 0;

  PlayerMuteData() {

  }

  public PlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean silent, boolean soft) {
    this.player = player;
    this.reason = reason;
    this.actor = actor;
    this.silent = silent;
    this.soft = soft;
  }

  public PlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean silent, boolean soft, long expires) {
    this(player, actor, reason, silent, soft);

    this.expires = expires;
  }

  public PlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean silent, boolean soft, long expires, boolean onlineOnly) {
    this(player, actor, reason, silent, soft, expires);

    this.onlineOnly = onlineOnly;
  }

  // Only use for imports!
  public PlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean silent, boolean soft, long expires, long created) {
    this(player, actor, reason, silent, soft, expires);

    this.created = created;
  }

  public PlayerMuteData(int id, PlayerData player, PlayerData actor, String reason, boolean silent, boolean soft, long expires, long created, long updated) {
    this(player, actor, reason, silent, soft, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public PlayerMuteData(int id, PlayerData player, PlayerData actor, String reason, boolean silent, boolean soft, long expires, long created, long updated, boolean onlineOnly, long pausedRemaining) {
    this(id, player, actor, reason, silent, soft, expires, created, updated);

    this.onlineOnly = onlineOnly;
    this.pausedRemaining = pausedRemaining;
  }

  public PlayerMuteData(PlayerMuteRecord record) {
    this(record.getPlayer(), record.getPastActor(), record.getReason(), record.isSilent(), record.isSoft(), record.getExpired(), record.getPastCreated());
    if (record.isOnlineOnly() && record.getRemainingOnlineTime() > 0) {
      this.onlineOnly = true;
      this.expires = 0;
      this.pausedRemaining = record.getRemainingOnlineTime();
    }
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }

  public boolean isPaused() {
    return onlineOnly && pausedRemaining > 0;
  }

  public void setExpires(long expires) {
    this.expires = expires;
  }

  public void setPausedRemaining(long pausedRemaining) {
    this.pausedRemaining = pausedRemaining;
  }

  public boolean equalsMute(PlayerMuteData mute) {
    return mute.getReason().equals(this.reason)
            && mute.getExpires() == expires
            && mute.getCreated() == this.created
            && mute.getPlayer().getUUID().equals(this.getPlayer().getUUID())
            && mute.getActor().getUUID().equals(this.actor.getUUID())
            && mute.isOnlineOnly() == this.onlineOnly
            && mute.getPausedRemaining() == this.pausedRemaining;
  }
}
