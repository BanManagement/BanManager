package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpRangeBanData;


public class IpRangeBannedEvent extends SilentEvent {

  @Getter
  private IpRangeBanData ban;

  public IpRangeBannedEvent(IpRangeBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
