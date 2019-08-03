package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;

public class IpUnbanEvent extends CommonCancellableEvent {

  @Getter
  private IpBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public IpUnbanEvent(IpBanData ban, PlayerData actor, String reason) {
    super(true);

    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }
}
