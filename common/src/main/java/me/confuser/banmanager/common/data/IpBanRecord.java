package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.storage.mysql.IpAddress;

@DatabaseTable
public class IpBanRecord {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(canBeNull = false, persisterClass = IpAddress.class, columnDefinition = "VARBINARY(16) NOT NULL")
  @Getter
  private IPAddress ip;

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

  @DatabaseField
  @Getter
  private boolean silent = false;

  IpBanRecord() {

  }

  public IpBanRecord(IpBanData ban, PlayerData actor, String reason) {
    ip = ban.getIp();
    expired = ban.getExpires();
    pastActor = ban.getActor();
    pastCreated = ban.getCreated();
    createdReason = reason;
    silent = ban.isSilent();

    this.reason = ban.getReason();
    this.actor = actor;
  }

  public IpBanRecord(IpBanData ban, PlayerData actor, long created) {
    ip = ban.getIp();
    reason = ban.getReason();
    expired = ban.getExpires();
    pastActor = ban.getActor();
    pastCreated = ban.getCreated();
    silent = ban.isSilent();

    this.actor = actor;
    this.created = created;
  }
}
