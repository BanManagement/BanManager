package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerBanData;


public class PlayerBannedEvent extends SilentEvent {

  @Getter
  private PlayerBanData ban;

  public PlayerBannedEvent(PlayerBanData ban, boolean isSilent) {
    super(isSilent);
    this.ban = ban;
  }

}
