package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;

public class PlayerUnbanEvent extends CommonCancellableEvent {

  @Getter
  private PlayerBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public PlayerUnbanEvent(PlayerBanData ban, PlayerData actor, String reason) {
    super(true);

    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }

}
