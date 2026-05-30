package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.WarnRequest;

import java.util.Objects;

/**
 * Pre-event for a player warning. Cancellable on every platform (in v7 this
 * was inconsistent: cancellable on Sponge, non-cancellable elsewhere).
 */
public final class PlayerWarnEvent extends AbstractCancellableEvent {

  private final WarnRequest request;

  public PlayerWarnEvent(WarnRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  public WarnRequest request() { return request; }
}
