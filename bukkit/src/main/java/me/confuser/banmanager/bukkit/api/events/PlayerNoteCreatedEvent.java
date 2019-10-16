package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerNoteData;


public class PlayerNoteCreatedEvent extends CustomCancellableEvent {

  @Getter
  private PlayerNoteData note;

  public PlayerNoteCreatedEvent(PlayerNoteData note) {
    super(true);
    this.note = note;
  }
}
