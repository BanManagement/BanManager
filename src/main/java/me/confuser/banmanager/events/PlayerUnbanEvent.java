package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerBanData;

public class PlayerUnbanEvent extends CustomCancellableEvent {

      @Getter
      private PlayerBanData ban;

      public PlayerUnbanEvent(PlayerBanData ban) {
            this.ban = ban;
      }

}
