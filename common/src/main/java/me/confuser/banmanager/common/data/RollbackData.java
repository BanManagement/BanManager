package me.confuser.banmanager.common.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
public class RollbackData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = false, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;

  @Getter
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData actor;

  @DatabaseField(canBeNull = false)
  @Getter
  private String type;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long expires = 0;

  RollbackData() {

  }

  public RollbackData(PlayerData player, PlayerData actor, String type, long expires) {
    this.player = player;
    this.actor = actor;
    this.type = type;

    this.expires = expires;
  }

  // Only use for imports!
  public RollbackData(PlayerData player, PlayerData actor, String type, long expires, long created) {
    this(player, actor, type, expires);

    this.created = created;
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }
}
