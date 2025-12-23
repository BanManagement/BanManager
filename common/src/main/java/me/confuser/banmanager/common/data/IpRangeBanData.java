package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.google.guava.collect.Range;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.storage.mysql.IpAddress;
import me.confuser.banmanager.common.util.IPUtils;

@DatabaseTable
public class IpRangeBanData {

  @Getter
  @DatabaseField(generatedId = true)
  private int id;

  @Getter
  @DatabaseField(canBeNull = false, persisterClass = IpAddress.class, columnDefinition = "VARBINARY(16) NOT NULL")
  private IPAddress fromIp;

  @Getter
  @DatabaseField(canBeNull = false, persisterClass = IpAddress.class, columnDefinition = "VARBINARY(16) NOT NULL")
  private IPAddress toIp;

  @Getter
  @DatabaseField(canBeNull = false)
  private String reason;

  @Getter
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData actor;

  // Should always be database time
  @Getter
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  private long created = System.currentTimeMillis() / 1000L;
  @Getter
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  private long updated = System.currentTimeMillis() / 1000L;
  @Getter
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  private long expires = 0;

  @DatabaseField
  @Getter
  private boolean silent = false;

  IpRangeBanData() {

  }

  public IpRangeBanData(IPAddress fromIp, IPAddress toIp, PlayerData actor, String reason, boolean silent) {
    this.fromIp = fromIp;
    this.toIp = toIp;
    this.reason = reason;
    this.actor = actor;
    this.silent = silent;
  }

  public IpRangeBanData(IPAddress fromIp, IPAddress toIp, PlayerData actor, String reason, boolean silent, long expires) {
    this(fromIp, toIp, actor, reason, silent);

    this.expires = expires;
  }

  public IpRangeBanData(IPAddress fromIp, IPAddress toIp, PlayerData actor, String reason, boolean silent, long expires, long created) {
    this(fromIp, toIp, actor, reason, silent, expires);

    this.created = created;
  }

  public IpRangeBanData(int id, IPAddress fromIp, IPAddress toIp, PlayerData actor, String reason, boolean silent, long expires, long created, long updated) {
    this(fromIp, toIp, actor, reason, silent, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public IpRangeBanData(IpRangeBanRecord banRecord) {
    this(banRecord.getFromIp(), banRecord.getToIp(), banRecord.getPastActor(), banRecord.getReason(), banRecord.isSilent(), banRecord.getExpired(), banRecord.getPastCreated());
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }

  public boolean inRange(IPAddress ip) {
    return IPUtils.isInRange(fromIp, toIp, ip);
  }

  public Range getRange() {
    return Range.closed(fromIp, toIp);
  }

  public boolean equalsBan(IpRangeBanData ban) {
    return ban.getReason().equals(this.reason)
            && ban.getExpires() == expires
            && ban.getCreated() == this.created
            && ban.getFromIp().equals(this.fromIp)
            && ban.getToIp().equals(this.toIp)
            && ban.getActor().getUUID().equals(this.actor.getUUID());
  }
}
