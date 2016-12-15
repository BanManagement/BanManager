package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.NameBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;

public class NameUnbanEvent extends CustomCancellableEvent {

  @Getter
  private NameBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public NameUnbanEvent(NameBanData ban, PlayerData actor, String reason) {
    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }

}
