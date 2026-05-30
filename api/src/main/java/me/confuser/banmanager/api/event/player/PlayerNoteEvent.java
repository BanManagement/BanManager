package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.NoteRequest;

import java.util.Objects;

public final class PlayerNoteEvent extends AbstractCancellableEvent {

  private final NoteRequest request;

  public PlayerNoteEvent(NoteRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  public NoteRequest request() { return request; }
}
