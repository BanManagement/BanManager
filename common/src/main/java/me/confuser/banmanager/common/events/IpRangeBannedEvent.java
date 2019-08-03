package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpRangeBanData;

public class IpRangeBannedEvent extends CommonSilentEvent {

  @Getter
  private IpRangeBanData ban;

  public IpRangeBannedEvent(IpRangeBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
