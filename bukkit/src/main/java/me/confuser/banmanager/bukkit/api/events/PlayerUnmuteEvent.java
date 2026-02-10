package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;


public class PlayerUnmuteEvent extends SilentCancellableEvent {

  @Getter
  private PlayerMuteData mute;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;
  public PlayerUnmuteEvent(PlayerMuteData mute, PlayerData actor, String reason) {
    this(mute, actor, reason, false);
  }

  public PlayerUnmuteEvent(PlayerMuteData mute, PlayerData actor, String reason, boolean silent) {
    super(silent, true);

    this.mute = mute;
    this.actor = actor;
    this.reason = reason;
  }
}
