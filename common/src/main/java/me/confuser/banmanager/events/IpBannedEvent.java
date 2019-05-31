package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpBanData;

public class IpBannedEvent extends SilentEvent {

  @Getter
  private IpBanData ban;

  public IpBannedEvent(IpBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
