package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpBanData;

public class IpUnbanEvent extends CustomCancellableEvent {

  @Getter
  private IpBanData ban;
  @Getter
  private String reason;

  public IpUnbanEvent(IpBanData ban, String reason) {
    this.ban = ban;
    this.reason = reason;
  }
}
