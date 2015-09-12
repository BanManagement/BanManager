package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;

public class PlayerUnbanEvent extends CustomCancellableEvent {

  @Getter
  private PlayerBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public PlayerUnbanEvent(PlayerBanData ban, PlayerData actor, String reason) {
    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }

}
