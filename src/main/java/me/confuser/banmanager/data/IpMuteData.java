package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;

@DatabaseTable
public class IpMuteData {

  @Getter
  @DatabaseField(generatedId = true)
  private int id;
  @Getter
  @DatabaseField(index = true, canBeNull = false, columnDefinition = "INT UNSIGNED NOT NULL")
  private long ip;
  @Getter
  @DatabaseField(canBeNull = false)
  private String reason;
  @Getter
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData actor;

  // Should always be database time
  @Getter
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  private long created = System.currentTimeMillis() / 1000L;
  @Getter
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  private long updated = System.currentTimeMillis() / 1000L;
  @Getter
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  private long expires = 0;

  @DatabaseField(index = true)
  @Getter
  private boolean soft = false;

  IpMuteData() {

  }

  public IpMuteData(long ip, PlayerData actor, String reason, boolean soft) {
    this.ip = ip;
    this.actor = actor;
    this.reason = reason;
    this.soft = soft;
  }

  public IpMuteData(long ip, PlayerData actor, String reason, boolean soft, long expires) {
    this(ip, actor, reason, soft);
    this.expires = expires;
  }

  public IpMuteData(long ip, PlayerData actor, String reason, boolean soft, long expires, long created) {
    this(ip, actor, reason, soft, expires);
    this.created = created;
  }

  public IpMuteData(int id, long ip, PlayerData actor, String reason, boolean soft, long expires, long created, long updated) {
    this(ip, actor, reason, soft, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }
}
