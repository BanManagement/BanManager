package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpRangeBanData;

public class IpRangeBannedEvent extends SilentEvent {

  @Getter
  private IpRangeBanData ban;

  public IpRangeBannedEvent(IpRangeBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
