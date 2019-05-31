package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.NameBanData;

public class NameBannedEvent extends SilentEvent {

  @Getter
  private NameBanData ban;

  public NameBannedEvent(NameBanData ban, boolean isSilent) {
    super(isSilent, true);
    this.ban = ban;
  }

}
