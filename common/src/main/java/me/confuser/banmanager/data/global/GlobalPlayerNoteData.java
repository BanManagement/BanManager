package me.confuser.banmanager.data.global;

import com.j256.ormlite.field.DatabaseField;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.banmanager.storage.PlayerStorage;
import me.confuser.banmanager.storage.mysql.ByteArray;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;

public class GlobalPlayerNoteData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(columnName = "uuid", canBeNull = false, index = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private byte[] uuidBytes;

  @DatabaseField(canBeNull = false, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String name;

  @DatabaseField(canBeNull = false)
  @Getter
  private String message;

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

  private PlayerStorage playerStorage = BanManager.getPlugin().getPlayerStorage();

  GlobalPlayerNoteData() {

  }

  public GlobalPlayerNoteData(PlayerData player, PlayerData actor, String message) {
    this.uuidBytes = player.getId();
    this.name = player.getName();
    this.message = message;
    this.actorUuidBytes = actor.getId();
    this.actorName = actor.getName();
  }

  // Only use for imports!
  public GlobalPlayerNoteData(PlayerData player, PlayerData actor, String message, long created) {
    this(player, actor, message);

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

  public PlayerData getPlayer() throws SQLException {
    return playerStorage.createIfNotExists(getUUID(), getName());
  }

  public PlayerData getActor() throws SQLException {
    return playerStorage.createIfNotExists(getActorUUID(), getActorName());
  }

  public PlayerNoteData toLocal() throws SQLException {
    return new PlayerNoteData(getPlayer(), getActor(), message, created);
  }

  public String getMessageColours() {
    return ChatColor.translateAlternateColorCodes('&', this.message);
  }
}
