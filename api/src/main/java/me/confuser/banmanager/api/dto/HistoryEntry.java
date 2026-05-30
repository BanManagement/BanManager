package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Cross-table history entry combining bans, mutes, warnings, kicks etc.
 * Returned by {@link me.confuser.banmanager.api.service.HistoryService}.
 *
 * @param id row id within the originating table
 * @param type one of {@code "ban"}, {@code "mute"}, {@code "warning"},
 *             {@code "kick"}, {@code "note"}, etc.
 * @param actor display name of the actor who took the action
 * @param created unix timestamp seconds when the action was taken
 * @param reason the reason or description
 * @param meta storage-specific metadata as a JSON-serialised string (may be
 *             empty)
 */
public record HistoryEntry(int id, String type, String actor, long created, String reason, String meta) {

  public HistoryEntry {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(meta, "meta");
  }
}
