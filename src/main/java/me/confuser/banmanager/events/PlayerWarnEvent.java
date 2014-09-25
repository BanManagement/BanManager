package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerWarnData;

public class PlayerWarnEvent extends CustomCancellableEvent {

      @Getter
      private PlayerWarnData warning;

      public PlayerWarnEvent(PlayerWarnData warning) {
            this.warning = warning;
      }
}
