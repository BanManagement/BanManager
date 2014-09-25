package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerBanData;

public class PlayerBanEvent extends CustomCancellableEvent {

      @Getter
      private PlayerBanData ban;

      public PlayerBanEvent(PlayerBanData ban) {
            this.ban = ban;
      }

}
