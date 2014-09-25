package me.confuser.banmanager.events;

import me.confuser.banmanager.data.IpBanData;

public class IpUnbanEvent extends CustomCancellableEvent {

      private IpBanData data;

      public IpUnbanEvent(IpBanData data) {
            this.data = data;
      }

      public IpBanData getBan() {
            return data;
      }

}
