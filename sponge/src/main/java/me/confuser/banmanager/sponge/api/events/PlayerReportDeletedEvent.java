package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerReportData;

public class PlayerReportDeletedEvent extends CustomEvent {

  @Getter
  private PlayerReportData report;

  public PlayerReportDeletedEvent(PlayerReportData report) {
    super();
    this.report = report;
  }
}
