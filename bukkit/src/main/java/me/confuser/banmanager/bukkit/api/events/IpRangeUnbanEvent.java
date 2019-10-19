package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;


public class IpRangeUnbanEvent extends CustomCancellableEvent {

  @Getter
  private IpRangeBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public IpRangeUnbanEvent(IpRangeBanData ban, PlayerData actor, String reason) {
    super(true);

    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }
}
