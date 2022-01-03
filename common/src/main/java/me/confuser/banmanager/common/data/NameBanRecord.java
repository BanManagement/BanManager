package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
public class NameBanRecord {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(canBeNull = false, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String name;

  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;

  @DatabaseField(canBeNull = false)
  @Getter
  private long expired;

  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;

  @Getter
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
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

  NameBanRecord() {

  }

  public NameBanRecord(NameBanData ban, PlayerData actor, String reason) {
    name = ban.getName();
    expired = ban.getExpires();
    pastActor = ban.getActor();
    pastCreated = ban.getCreated();
    createdReason = reason;
    silent = ban.isSilent();

    this.reason = ban.getReason();
    this.actor = actor;
  }

  public NameBanRecord(NameBanData ban, PlayerData actor, long created) {
    name = ban.getName();
    reason = ban.getReason();
    expired = ban.getExpires();
    pastActor = ban.getActor();
    pastCreated = ban.getCreated();

    silent = ban.isSilent();

    this.actor = actor;
    this.created = created;
  }

  public boolean equalsBan(NameBanData ban) {
    return ban.getReason().equals(this.reason)
        && ban.getExpires() == expired
        && ban.getCreated() == this.pastCreated
        && ban.getName().equals(this.getName())
        && ban.getActor().getUUID().equals(this.pastActor.getUUID());
  }
}
