package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerReportData;

public class PlayerReportedEvent extends SilentEvent {

  @Getter
  private PlayerReportData report;

  public PlayerReportedEvent(PlayerReportData report, boolean isSilent) {
    super(isSilent, true);
    this.report = report;
  }

}
