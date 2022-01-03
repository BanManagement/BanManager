package me.confuser.banmanager.common.data.global;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.storage.mysql.IpAddress;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;

public class GlobalIpBanData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @Getter
  @DatabaseField(canBeNull = false, persisterClass = IpAddress.class, columnDefinition = "VARBINARY(16) NOT NULL")
  private IPAddress ip;

  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;

  @DatabaseField(columnName = "actorUuid", canBeNull = false, index = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private byte[] actorUuidBytes;

  @DatabaseField(canBeNull = false, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String actorName;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long expires = 0;

  private UUID actorUUID;

  GlobalIpBanData() {

  }

  public GlobalIpBanData(IPAddress ip, PlayerData actor, String reason) {
    this.ip = ip;
    this.reason = reason;
    this.actorUuidBytes = actor.getId();
    this.actorName = actor.getName();
  }

  public GlobalIpBanData(IPAddress ip, PlayerData actor, String reason, long expires) {
    this(ip, actor, reason);

    this.expires = expires;
  }

  public UUID getActorUUID() {
    if (actorUUID == null) {
      actorUUID = UUIDUtils.fromBytes(actorUuidBytes);
    }

    return actorUUID;
  }

  public PlayerData getActor(BanManagerPlugin plugin) throws SQLException {
    return plugin.getPlayerStorage().createIfNotExists(getActorUUID(), getActorName());
  }

  public IpBanData toLocal(BanManagerPlugin plugin) throws SQLException {
    return new IpBanData(ip, getActor(plugin), reason, false, expires, created);
  }
}
