package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpRangeBanData;

public class IpRangeBanEvent extends SilentCancellableEvent {

  @Getter
  private IpRangeBanData ban;

  public IpRangeBanEvent(IpRangeBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
