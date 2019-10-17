package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpBanData;

public class IpBanEvent extends SilentCancellableEvent {

  @Getter
  private IpBanData ban;

  public IpBanEvent(IpBanData ban, boolean silent) {
    super(silent);

    this.ban = ban;
  }
}
