package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpBanData;

public class IpUnbanEvent extends CustomCancellableEvent {

      @Getter
      private IpBanData ban;

      public IpUnbanEvent(IpBanData ban) {
            this.ban = ban;
      }
}
