package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;


public class NameUnbanEvent extends CustomCancellableEvent {

  @Getter
  private NameBanData ban;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public NameUnbanEvent(NameBanData ban, PlayerData actor, String reason) {
    super();

    this.ban = ban;
    this.actor = actor;
    this.reason = reason;
  }

}
