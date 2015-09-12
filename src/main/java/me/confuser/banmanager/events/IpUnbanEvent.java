package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;

public class IpUnbanEvent extends CustomCancellableEvent {

  @Getter
  private IpBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public IpUnbanEvent(IpBanData ban, PlayerData actor, String reason) {
    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }
}
