package me.confuser.banmanager.common.data;

import com.google.common.collect.Range;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
public class IpRangeBanData {

  @Getter
  @DatabaseField(generatedId = true)
  private int id;

  @Getter
  @DatabaseField(canBeNull = false, columnDefinition = "INT UNSIGNED NOT NULL", index = true)
  private long fromIp;

  @Getter
  @DatabaseField(canBeNull = false, columnDefinition = "INT UNSIGNED NOT NULL", index = true)
  private long toIp;

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

  IpRangeBanData() {

  }

  public IpRangeBanData(long fromIp, long toIp, PlayerData actor, String reason) {
    this.fromIp = fromIp;
    this.toIp = toIp;
    this.reason = reason;
    this.actor = actor;
  }

  public IpRangeBanData(long fromIp, long toIp, PlayerData actor, String reason, long expires) {
    this(fromIp, toIp, actor, reason);

    this.expires = expires;
  }

  public IpRangeBanData(long fromIp, long toIp, PlayerData actor, String reason, long expires, long created) {
    this(fromIp, toIp, actor, reason, expires);

    this.created = created;
  }

  public IpRangeBanData(int id, long fromIp, long toIp, PlayerData actor, String reason, long expires, long created, long updated) {
    this(fromIp, toIp, actor, reason, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }

  public boolean inRange(long ip) {
    return ip > fromIp && ip < toIp;
  }

  public Range getRange() {
    return Range.closed(fromIp, toIp);
  }

  public boolean equalsBan(IpRangeBanData ban) {
    return ban.getReason().equals(this.reason)
            && ban.getExpires() == expires
            && ban.getCreated() == this.created
            && ban.getFromIp() == this.fromIp
            && ban.getToIp() == this.toIp
            && ban.getActor().getUUID().equals(this.actor.getUUID());
  }
}
