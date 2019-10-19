package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpBanData;

public class IpBannedEvent extends SilentEvent {

  @Getter
  private IpBanData ban;

  public IpBannedEvent(IpBanData ban, boolean silent) {
    super(silent, true);
    this.ban = ban;
  }
}
