package me.confuser.banmanager.events;

import me.confuser.banmanager.data.PlayerBanData;

public class PlayerBanEvent extends CustomCancellableEvent {

      private PlayerBanData data;

      public PlayerBanEvent(PlayerBanData data) {
            this.data = data;
      }

      public PlayerBanData getBan() {
            return data;
      }

}
