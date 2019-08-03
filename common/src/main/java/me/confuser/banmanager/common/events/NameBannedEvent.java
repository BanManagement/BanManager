package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.NameBanData;

public class NameBannedEvent extends CommonSilentEvent {

  @Getter
  private NameBanData ban;

  public NameBannedEvent(NameBanData ban, boolean isSilent) {
    super(isSilent, true);
    this.ban = ban;
  }

}
