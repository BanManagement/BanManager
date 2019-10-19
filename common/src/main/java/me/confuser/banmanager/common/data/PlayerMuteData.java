package me.confuser.banmanager.common.data;

import me.confuser.banmanager.common.storage.mysql.ByteArray;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

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
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long updated = System.currentTimeMillis() / 1000L;
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long expires = 0;


  @DatabaseField(index = true)
  @Getter
  private boolean soft = false;

  PlayerMuteData() {

  }

  public PlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean soft) {
    this.player = player;
    this.reason = reason;
    this.actor = actor;
    this.soft = soft;
  }

  public PlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean soft, long expires) {
    this(player, actor, reason, soft);

    this.expires = expires;
  }

  // Only use for imports!
  public PlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean soft, long expires, long created) {
    this(player, actor, reason, soft, expires);

    this.created = created;
  }

  public PlayerMuteData(int id, PlayerData player, PlayerData actor, String reason, boolean soft, long expires, long created, long updated) {
    this(player, actor, reason, soft, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public PlayerMuteData(PlayerMuteRecord record) {
    this(record.getPlayer(), record.getPastActor(), record.getReason(), record.isSoft(), record.getExpired(), record.getPastCreated());
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
