package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerBanData;

public class PlayerBanEvent extends SilentCancellableEvent {

  @Getter
  private PlayerBanData ban;

  public PlayerBanEvent(PlayerBanData ban, boolean isSilent) {
    super(isSilent, true);
    this.ban = ban;
  }

}
