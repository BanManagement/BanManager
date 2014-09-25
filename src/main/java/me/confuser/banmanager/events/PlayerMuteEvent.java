package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerMuteData;

public class PlayerMuteEvent extends CustomCancellableEvent {

      @Getter
      private PlayerMuteData mute;

      public PlayerMuteEvent(PlayerMuteData mute) {
            this.mute = mute;
      }
}
