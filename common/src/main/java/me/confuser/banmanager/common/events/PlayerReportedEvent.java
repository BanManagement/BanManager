package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerReportData;

public class PlayerReportedEvent extends CommonSilentEvent {

  @Getter
  private PlayerReportData report;

  public PlayerReportedEvent(PlayerReportData report, boolean isSilent) {
    super(isSilent, true);
    this.report = report;
  }

}
