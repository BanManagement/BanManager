package me.confuser.banmanager.data.external;

import com.j256.ormlite.field.DatabaseField;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.storage.PlayerStorage;
import me.confuser.banmanager.storage.mysql.ByteArray;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;

public class ExternalIpBanRecordData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @Getter
  @DatabaseField(canBeNull = false, columnDefinition = "INT UNSIGNED NOT NULL")
  private long ip;

  @DatabaseField(columnName = "actorUuid", canBeNull = false, index = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private byte[] actorUuidBytes;

  @DatabaseField(canBeNull = false, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String actorName;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  private UUID actorUUID;

  private PlayerStorage playerStorage = BanManager.getPlugin().getPlayerStorage();

  ExternalIpBanRecordData() {

  }

  public ExternalIpBanRecordData(long ip, PlayerData actor) {
    this.ip = ip;
    this.actorUuidBytes = actor.getId();
    this.actorName = actor.getName();
  }

  // Only use for imports!
  public ExternalIpBanRecordData(long ip, PlayerData actor, long created) {
    this(ip, actor);

    this.created = created;
  }

  public UUID getActorUUID() {
    if (actorUUID == null) {
      actorUUID = UUIDUtils.fromBytes(actorUuidBytes);
    }

    return actorUUID;
  }

  public PlayerData getActor() throws SQLException {
    return playerStorage.createIfNotExists(getActorUUID(), getActorName());
  }
}
