package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerBanData;

public class PlayerUnbanEvent extends CustomCancellableEvent {

      @Getter
      private PlayerBanData ban;
      @Getter
      private String reason;

      public PlayerUnbanEvent(PlayerBanData ban, String reason) {
            this.ban = ban;
            this.reason = reason;
      }

}
