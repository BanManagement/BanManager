package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerBanRecord {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;
  @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;
  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;
  @Getter
  @DatabaseField(canBeNull = false)
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
  @DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  PlayerBanRecord() {

  }

  public PlayerBanRecord(PlayerBanData ban, PlayerData actor) {
    player = ban.getPlayer();
    reason = ban.getReason();
    expired = ban.getExpires();
    pastActor = ban.getActor();
    pastCreated = ban.getCreated();

    this.actor = actor;
  }

  public PlayerBanRecord(PlayerBanData ban, PlayerData actor, long created) {
    player = ban.getPlayer();
    reason = ban.getReason();
    expired = ban.getExpires();
    pastActor = ban.getActor();
    pastCreated = ban.getCreated();

    this.actor = actor;
    this.created = created;
  }

  public boolean equalsBan(PlayerBanData ban) {
    return ban.getReason().equals(this.reason)
            && ban.getExpires() == expired
            && ban.getCreated() == this.pastCreated
            && ban.getPlayer().getUUID().equals(this.getPlayer().getUUID())
            && ban.getActor().getUUID().equals(this.pastActor.getUUID());
  }
}
