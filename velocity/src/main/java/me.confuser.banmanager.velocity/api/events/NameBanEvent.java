package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.NameBanData;


public class NameBanEvent extends SilentCancellableEvent {

  @Getter
  private NameBanData ban;

  public NameBanEvent(NameBanData ban, boolean isSilent) {
    super(isSilent);
    this.ban = ban;
  }

}
