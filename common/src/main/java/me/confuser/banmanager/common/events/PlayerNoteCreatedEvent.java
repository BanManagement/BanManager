package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerNoteData;

public class PlayerNoteCreatedEvent extends CommonCancellableEvent {

  @Getter
  private PlayerNoteData note;

  public PlayerNoteCreatedEvent(PlayerNoteData note) {
    super(true);
    this.note = note;
  }
}
