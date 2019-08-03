package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpBanData;

public class IpBannedEvent extends CommonSilentEvent {

  @Getter
  private IpBanData ban;

  public IpBannedEvent(IpBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
