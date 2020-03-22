package me.confuser.banmanager.common.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import inet.ipaddr.IPAddress;
import lombok.Getter;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.storage.mysql.IpAddress;

@DatabaseTable
public class IpMuteData {

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

  @DatabaseField(index = true)
  @Getter
  private boolean soft = false;

  @DatabaseField
  @Getter
  private boolean silent = false;

  IpMuteData() {

  }

  public IpMuteData(IPAddress ip, PlayerData actor, String reason, boolean silent, boolean soft) {
    this.ip = ip;
    this.actor = actor;
    this.reason = reason;
    this.silent = silent;
    this.soft = soft;
  }

  public IpMuteData(IPAddress ip, PlayerData actor, String reason, boolean silent, boolean soft, long expires) {
    this(ip, actor, reason, silent, soft);
    this.expires = expires;
  }

  public IpMuteData(IPAddress ip, PlayerData actor, String reason, boolean silent, boolean soft, long expires, long created) {
    this(ip, actor, reason, silent, soft, expires);
    this.created = created;
  }

  public IpMuteData(int id, IPAddress ip, PlayerData actor, String reason, boolean silent, boolean soft, long expires, long created, long updated) {
    this(ip, actor, reason, silent, soft, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public IpMuteData(IpMuteRecord record) {
    this(record.getIp(), record.getPastActor(), record.getReason(), record.isSilent(), record.isSoft(), record.getExpired(), record.getPastCreated());
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }

  public boolean equalsMute(IpMuteData mute) {
    return mute.getReason().equals(this.reason)
        && mute.getExpires() == expires
        && mute.getCreated() == this.created
        && mute.getIp().equals(this.ip)
        && mute.getActor().getUUID().equals(this.actor.getUUID());
  }
}
