package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class PlayerReportDeletedEvent implements BanManagerEvent {

  private final PlayerReport report;
  private final Player actor;

  public PlayerReportDeletedEvent(PlayerReport report, Player actor) {
    this.report = Objects.requireNonNull(report, "report");
    this.actor = Objects.requireNonNull(actor, "actor");
  }

  public PlayerReport report() { return report; }
  public Player actor() { return actor; }
}
