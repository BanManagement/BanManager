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

  public PlayerMuteData(PlayerMuteRecord record) {
    this(record.getPlayer(), record.getPastActor(), record.getReason(), record.isSilent(), record.isSoft(), record.getExpired(), record.getPastCreated());
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }

  public boolean equalsMute(PlayerMuteData mute) {
    return mute.getReason().equals(this.reason)
            && mute.getExpires() == expires
            && mute.getCreated() == this.created
            && mute.getPlayer().getUUID().equals(this.getPlayer().getUUID())
            && mute.getActor().getUUID().equals(this.actor.getUUID());
  }
}
