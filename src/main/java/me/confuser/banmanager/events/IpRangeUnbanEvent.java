package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpRangeBanData;

public class IpRangeUnbanEvent extends CustomCancellableEvent {

  @Getter
  private IpRangeBanData ban;
  @Getter
  private String reason;

  public IpRangeUnbanEvent(IpRangeBanData ban, String reason) {
    this.ban = ban;
    this.reason = reason;
  }
}
