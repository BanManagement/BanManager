package me.confuser.banmanager.events;

import me.confuser.banmanager.data.IpBanData;

public class IpBanEvent extends CustomCancellableEvent {

      private IpBanData data;

      public IpBanEvent(IpBanData data) {
            this.data = data;
      }

      public IpBanData getBan() {
            return data;
      }

}
