package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpRangeBanData;

public class IpRangeUnbanEvent extends CustomCancellableEvent {

  @Getter
  private IpRangeBanData ban;

  public IpRangeUnbanEvent(IpRangeBanData ban) {
    this.ban = ban;
  }
}
