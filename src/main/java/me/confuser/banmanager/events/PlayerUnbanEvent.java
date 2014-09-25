package me.confuser.banmanager.events;

import me.confuser.banmanager.data.PlayerBanData;

public class PlayerUnbanEvent extends CustomCancellableEvent {

      private PlayerBanData data;

      public PlayerUnbanEvent(PlayerBanData data) {
            this.data = data;
      }

      public PlayerBanData getBan() {
            return data;
      }

}
