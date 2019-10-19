package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerReportData;


public class PlayerReportEvent extends SilentCancellableEvent {

  @Getter
  private PlayerReportData report;

  public PlayerReportEvent(PlayerReportData report, boolean isSilent) {
    super(isSilent, true);
    this.report = report;
  }

}
