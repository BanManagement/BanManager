package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerWarnData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;

  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;

  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long expires = 0;

  @DatabaseField(index = true)
  @Getter
  @Setter
  private boolean read = true;

  @DatabaseField(index = true)
  @Getter
  private int points = 1;

  PlayerWarnData() {

  }

  public PlayerWarnData(PlayerData player, PlayerData actor, String reason, int points) {
    this.player = player;
    this.reason = reason;
    this.actor = actor;
    this.points = points;
  }

  public PlayerWarnData(PlayerData player, PlayerData actor, String reason, int points, boolean read) {
    this(player, actor, reason, points);

    this.read = read;
  }

  public PlayerWarnData(PlayerData player, PlayerData actor, String reason, int points, boolean read, long expires) {
    this(player, actor, reason, points, read);

    this.expires = expires;
  }

  // Imports only!
  public PlayerWarnData(PlayerData player, PlayerData actor, String reason, boolean read, long expires, long created) {
    this(player, actor, reason, 1, read, expires);

    this.created = created;
  }

}
