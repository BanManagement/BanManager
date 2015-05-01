package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpRangeBanData;

public class IpRangeBanEvent extends CustomCancellableEvent {

  @Getter
  private IpRangeBanData ban;

  public IpRangeBanEvent(IpRangeBanData ban) {
    this.ban = ban;
  }
}
