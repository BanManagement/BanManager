package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerReportData;


public class PlayerReportedEvent extends SilentEvent {

  @Getter
  private PlayerReportData report;

  public PlayerReportedEvent(PlayerReportData report, boolean isSilent) {
    super(isSilent);
    this.report = report;
  }

}
