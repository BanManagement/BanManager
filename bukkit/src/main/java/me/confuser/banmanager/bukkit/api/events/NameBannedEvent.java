package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.NameBanData;

public class NameBannedEvent extends SilentEvent {

  @Getter
  private NameBanData ban;

  public NameBannedEvent(NameBanData ban, boolean isSilent) {
    super(isSilent, true);
    this.ban = ban;
  }

}
