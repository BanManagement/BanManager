package me.confuser.banmanager.common.data;

public record HistoryEntry(int id, String type, String actor, long created, String reason, String meta) {
}
