package me.confuser.banmanager.api.event.name;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.NameBanRequest;

import java.util.Objects;

public final class NameBanEvent extends AbstractCancellableEvent {

  private final NameBanRequest request;

  public NameBanEvent(NameBanRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  public NameBanRequest request() { return request; }
}
