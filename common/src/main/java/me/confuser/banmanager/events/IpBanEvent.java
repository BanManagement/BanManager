package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpBanData;

public class IpBanEvent extends SilentCancellableEvent {

  @Getter
  private IpBanData ban;

  public IpBanEvent(IpBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
