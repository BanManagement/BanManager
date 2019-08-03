package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerBanData;

public class PlayerBanEvent extends CommonSilentCancellableEvent {

  @Getter
  private PlayerBanData ban;

  public PlayerBanEvent(PlayerBanData ban, boolean isSilent) {
    super(isSilent, true);
    this.ban = ban;
  }

}
