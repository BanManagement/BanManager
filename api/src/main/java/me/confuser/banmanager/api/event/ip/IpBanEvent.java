package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.IpBanRequest;

import java.util.Objects;

public final class IpBanEvent extends AbstractCancellableEvent {

  private final IpBanRequest request;

  public IpBanEvent(IpBanRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  public IpBanRequest request() { return request; }
}
