package me.confuser.banmanager.common.data;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.common.storage.mysql.IpAddress;

@DatabaseTable
public class IpBanData {

  @Getter
  @DatabaseField(generatedId = true)
  private int id;
  @Getter
  @DatabaseField(canBeNull = false, persisterClass = IpAddress.class, columnDefinition = "VARBINARY(16) NOT NULL")
  private IPAddress ip;
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

  @DatabaseField
  @Getter
  private boolean silent = false;

  IpBanData() {

  }

  public IpBanData(IPAddress ip, PlayerData actor, String reason, boolean silent) {
    this.ip = ip;
    this.reason = reason;
    this.actor = actor;
    this.silent = silent;
  }

  public IpBanData(IPAddress ip, PlayerData actor, String reason, boolean silent, long expires) {
    this(ip, actor, reason, silent);

    this.expires = expires;
  }

  public IpBanData(IPAddress ip, PlayerData actor, String reason, boolean silent, long expires, long created) {
    this(ip, actor, reason, silent, expires);

    this.created = created;
  }

  public IpBanData(int id, IPAddress ip, PlayerData actor, String reason, boolean silent, long expires, long created, long updated) {
    this(ip, actor, reason, silent, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public IpBanData(IpBanRecord record) {
    this(record.getIp(), record.getPastActor(), record.getReason(), record.isSilent(), record.getExpired(), record.getPastCreated());
  }

  public boolean hasExpired() {
        return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }

  public boolean equalsBan(IpBanData ban) {
    return ban.getReason().equals(this.reason)
            && ban.getExpires() == expires
            && ban.getCreated() == this.created
            && ban.getIp().equals(this.ip)
            && ban.getActor().getUUID().equals(this.actor.getUUID());
  }
}
