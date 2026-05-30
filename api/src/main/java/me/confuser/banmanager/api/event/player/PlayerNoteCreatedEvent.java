package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.PlayerNote;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class PlayerNoteCreatedEvent implements BanManagerEvent {

  private final PlayerNote note;

  public PlayerNoteCreatedEvent(PlayerNote note) {
    this.note = Objects.requireNonNull(note, "note");
  }

  public PlayerNote note() { return note; }
}
