package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.BanRequest;

import java.util.Objects;

/**
 * Pre-event fired before a player ban is persisted. Handlers may mutate the
 * {@link #request()} ({@code reason}, {@code expires}, {@code silent}) or
 * {@link #cancel()} the ban entirely.
 */
public final class PlayerBanEvent extends AbstractCancellableEvent {

  private final BanRequest request;

  public PlayerBanEvent(BanRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  /**
   * @return the mutable ban request that will be persisted if no handler
   *         calls {@link #cancel()}
   */
  public BanRequest request() {
    return request;
  }
}
