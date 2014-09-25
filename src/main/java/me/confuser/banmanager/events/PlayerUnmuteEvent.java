package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerMuteData;

public class PlayerUnmuteEvent extends CustomCancellableEvent {

      @Getter
      private PlayerMuteData mute;

      public PlayerUnmuteEvent(PlayerMuteData mute) {
            this.mute = mute;
      }
}
