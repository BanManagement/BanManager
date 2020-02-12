package me.confuser.banmanager.common.data;

import com.google.common.collect.Range;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import inet.ipaddr.IPAddress;
import lombok.Getter;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.storage.mysql.IpAddress;

@DatabaseTable
public class IpRangeBanRecord {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(canBeNull = false, persisterClass = IpAddress.class, columnDefinition = "VARBINARY(16) NOT NULL", index = true)
  @Getter
  private IPAddress fromIp;

  @DatabaseField(canBeNull = false, persisterClass = IpAddress.class, columnDefinition = "VARBINARY(16) NOT NULL", index = true)
  @Getter
  private IPAddress toIp;

  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;

  @DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long expired;

  @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;

  @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData pastActor;

  @DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long pastCreated;

  @DatabaseField(index = true, canBeNull = false, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  @DatabaseField(canBeNull = false)
  @Getter
  private String createdReason;

  @DatabaseField
  @Getter
  private boolean silent = false;

  IpRangeBanRecord() {

  }

  public IpRangeBanRecord(IpRangeBanData ban, PlayerData actor, String reason) {
    fromIp = ban.getFromIp();
    toIp = ban.getToIp();
    expired = ban.getExpires();
    pastActor = ban.getActor();
    pastCreated = ban.getCreated();
    createdReason = reason;
    silent = ban.isSilent();

    this.reason = ban.getReason();
    this.actor = actor;
  }

  public IpRangeBanRecord(IpRangeBanData ban, PlayerData actor, long created) {
    this(ban, actor, "");
    this.created = created;
  }

  public Range getRange() {
    return Range.closed(fromIp, toIp);
  }
}
