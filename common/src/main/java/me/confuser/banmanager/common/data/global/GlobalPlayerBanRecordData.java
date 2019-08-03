package me.confuser.banmanager.common.data.global;

import com.j256.ormlite.field.DatabaseField;
import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.storage.PlayerStorage;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;

public class GlobalPlayerBanRecordData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(columnName = "uuid", canBeNull = false, index = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private byte[] uuidBytes;

  @DatabaseField(canBeNull = false, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String name;

  @DatabaseField(columnName = "actorUuid", canBeNull = false, index = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private byte[] actorUuidBytes;

  @DatabaseField(canBeNull = false, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String actorName;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  private UUID uuid;
  private UUID actorUUID;

  GlobalPlayerBanRecordData() {

  }

  public GlobalPlayerBanRecordData(PlayerData player, PlayerData actor) {
    this.uuidBytes = player.getId();
    this.name = player.getName();
    this.actorUuidBytes = actor.getId();
    this.actorName = actor.getName();
  }

  // Only use for imports!
  public GlobalPlayerBanRecordData(PlayerData player, PlayerData actor, long created) {
    this(player, actor);

    this.created = created;
  }

  public UUID getUUID() {
    if (uuid == null) {
      uuid = UUIDUtils.fromBytes(uuidBytes);
    }

    return uuid;
  }

  public UUID getActorUUID() {
    if (actorUUID == null) {
      actorUUID = UUIDUtils.fromBytes(actorUuidBytes);
    }

    return actorUUID;
  }

  public PlayerData getPlayer(BanManagerPlugin plugin) throws SQLException {
    return plugin.getPlayerStorage().createIfNotExists(getUUID(), getName());
  }

  public PlayerData getActor(BanManagerPlugin plugin) throws SQLException {
    return plugin.getPlayerStorage().createIfNotExists(getActorUUID(), getActorName());
  }
}
