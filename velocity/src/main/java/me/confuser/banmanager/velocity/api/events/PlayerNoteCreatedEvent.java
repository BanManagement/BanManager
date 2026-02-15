package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerNoteData;


public class PlayerNoteCreatedEvent extends SilentCancellableEvent {

  @Getter
  private PlayerNoteData note;

  public PlayerNoteCreatedEvent(PlayerNoteData note) {
    this(note, false);
  }

  public PlayerNoteCreatedEvent(PlayerNoteData note, boolean silent) {
    super(silent);
    this.note = note;
  }
}
