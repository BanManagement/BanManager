package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.NameBanData;

public class NameBannedEvent extends SilentEvent {

  @Getter
  private NameBanData ban;

  public NameBannedEvent(NameBanData ban, boolean isSilent) {
    super(isSilent);
    this.ban = ban;
  }

}
