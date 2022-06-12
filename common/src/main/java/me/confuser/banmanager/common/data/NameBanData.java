package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
public class NameBanData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(unique = true, canBeNull = false, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String name;

  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;

  @Getter
  @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private PlayerData actor;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long updated = System.currentTimeMillis() / 1000L;

  @DatabaseField(index = true, columnDefinition = "BIGINT UNSIGNED NOT NULL")
  @Getter
  private long expires = 0;

  @DatabaseField
  @Getter
  private boolean silent = false;

  NameBanData() {

  }

  public NameBanData(String name, PlayerData actor, String reason, boolean silent) {
    this.name = name;
    this.reason = reason;
    this.actor = actor;
    this.silent = silent;
  }

  public NameBanData(String name, PlayerData actor, String reason, boolean silent, long expires) {
    this(name, actor, reason, silent);

    this.expires = expires;
  }

  // Only use for imports!
  public NameBanData(String name, PlayerData actor, String reason, boolean silent, long expires, long created) {
    this(name, actor, reason, silent, expires);

    this.created = created;
  }

  public NameBanData(int id, String name, PlayerData actor, String reason, boolean silent, long expires, long created, long updated) {
    this(name, actor, reason, silent, expires, created);

    this.id = id;
    this.updated = updated;
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }
}
