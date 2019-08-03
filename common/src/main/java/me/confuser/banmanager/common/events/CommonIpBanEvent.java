package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpBanData;

public class CommonIpBanEvent extends CommonSilentCancellableEvent {

  @Getter
  private IpBanData ban;

  public CommonIpBanEvent(IpBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
