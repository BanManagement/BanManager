package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerBanData;

public class PlayerBannedEvent extends CommonSilentEvent {

  @Getter
  private PlayerBanData ban;

  public PlayerBannedEvent(PlayerBanData ban, boolean isSilent) {
    super(isSilent, true);
    this.ban = ban;
  }

}
