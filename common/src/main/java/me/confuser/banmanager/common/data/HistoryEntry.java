package me.confuser.banmanager.common.data;

import lombok.Getter;

public class HistoryEntry {

  @Getter
  private final int id;

  @Getter
  private final String type;

  @Getter
  private final String actor;

  @Getter
  private final long created;

  @Getter
  private final String reason;

  @Getter
  private final String meta;

  public HistoryEntry(int id, String type, String actor, long created, String reason, String meta) {
    this.id = id;
    this.type = type;
    this.actor = actor;
    this.created = created;
    this.reason = reason;
    this.meta = meta;
  }
}
