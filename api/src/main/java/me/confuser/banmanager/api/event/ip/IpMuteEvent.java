package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.IpMuteRequest;

import java.util.Objects;

public final class IpMuteEvent extends AbstractCancellableEvent {

  private final IpMuteRequest request;

  public IpMuteEvent(IpMuteRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  public IpMuteRequest request() { return request; }
}
