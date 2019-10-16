package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;


public class IpUnmutedEvent extends CustomCancellableEvent {

  @Getter
  private IpMuteData mute;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public IpUnmutedEvent(IpMuteData mute, PlayerData actor, String reason) {
    super(true);

    this.mute = mute;
    this.actor = actor;
    this.reason = reason;
  }
}
