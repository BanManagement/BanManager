package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.NameBanData;

public class NameBanEvent extends SilentCancellableEvent {

  @Getter
  private NameBanData ban;

  public NameBanEvent(NameBanData ban, boolean isSilent) {
    super(isSilent, true);
    this.ban = ban;
  }

}
