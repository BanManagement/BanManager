package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.IpRangeBanRequest;

import java.util.Objects;

public final class IpRangeBanEvent extends AbstractCancellableEvent {

  private final IpRangeBanRequest request;

  public IpRangeBanEvent(IpRangeBanRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  public IpRangeBanRequest request() { return request; }
}
