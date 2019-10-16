package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerReportData;

public class PlayerReportDeletedEvent extends CustomEvent {

  @Getter
  private PlayerReportData report;

  public PlayerReportDeletedEvent(PlayerReportData report) {
    super(true);
    this.report = report;
  }
}
