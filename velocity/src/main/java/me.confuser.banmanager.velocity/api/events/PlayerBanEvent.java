package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerBanData;


public class PlayerBanEvent extends SilentCancellableEvent {

  @Getter
  private PlayerBanData ban;

  public PlayerBanEvent(PlayerBanData ban, boolean isSilent) {
    super(isSilent);
    this.ban = ban;
  }

}
