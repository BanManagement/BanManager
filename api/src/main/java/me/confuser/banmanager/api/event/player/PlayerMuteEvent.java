package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.MuteRequest;

import java.util.Objects;

public final class PlayerMuteEvent extends AbstractCancellableEvent {

  private final MuteRequest request;

  public PlayerMuteEvent(MuteRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  public MuteRequest request() { return request; }
}
