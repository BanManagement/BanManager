package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.NameBanData;

public class NameBanEvent extends CommonSilentCancellableEvent {

  @Getter
  private NameBanData ban;

  public NameBanEvent(NameBanData ban, boolean isSilent) {
    super(isSilent, true);
    this.ban = ban;
  }

}
