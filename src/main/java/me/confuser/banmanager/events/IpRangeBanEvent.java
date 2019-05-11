package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpRangeBanData;

public class IpRangeBanEvent extends SilentCancellableEvent {

  @Getter
  private IpRangeBanData ban;

  public IpRangeBanEvent(IpRangeBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
