package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerData;

public class IpRangeUnbanEvent extends CustomCancellableEvent {

  @Getter
  private IpRangeBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public IpRangeUnbanEvent(IpRangeBanData ban, PlayerData actor, String reason) {
    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }
}
