package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerNoteData;

public class PlayerNoteCreatedEvent extends CustomCancellableEvent {

  @Getter
  private PlayerNoteData note;

  public PlayerNoteCreatedEvent(PlayerNoteData note) {
    this.note = note;
  }
}
