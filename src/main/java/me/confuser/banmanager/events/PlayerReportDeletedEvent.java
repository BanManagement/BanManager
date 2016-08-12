package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;

public class PlayerReportDeletedEvent extends CustomEvent {

  @Getter
  private PlayerReportData report;

  public PlayerReportDeletedEvent(PlayerReportData report) {
    this.report = report;
  }
}
