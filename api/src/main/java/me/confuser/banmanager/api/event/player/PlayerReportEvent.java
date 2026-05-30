package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.request.ReportRequest;

import java.util.Objects;

public final class PlayerReportEvent extends AbstractCancellableEvent {

  private final ReportRequest request;

  public PlayerReportEvent(ReportRequest request) {
    this.request = Objects.requireNonNull(request, "request");
  }

  public ReportRequest request() { return request; }
}
