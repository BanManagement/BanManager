package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;


public class IpUnbanEvent extends SilentCancellableEvent {

  @Getter
  private IpBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;
  public IpUnbanEvent(IpBanData ban, PlayerData actor, String reason) {
    this(ban, actor, reason, false);
  }

  public IpUnbanEvent(IpBanData ban, PlayerData actor, String reason, boolean silent) {
    super(silent, true);

    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }
}
