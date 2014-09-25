package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpBanData;

public class IpBanEvent extends CustomCancellableEvent {

      @Getter
      private IpBanData ban;

      public IpBanEvent(IpBanData ban) {
            this.ban = ban;
      }
}
