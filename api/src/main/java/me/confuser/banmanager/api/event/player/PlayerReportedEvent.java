package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class PlayerReportedEvent implements BanManagerEvent {

  private final PlayerReport report;

  public PlayerReportedEvent(PlayerReport report) {
    this.report = Objects.requireNonNull(report, "report");
  }

  public PlayerReport report() { return report; }
}
