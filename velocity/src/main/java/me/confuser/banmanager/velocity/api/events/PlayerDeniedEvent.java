package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

public class PlayerDeniedEvent extends CustomCancellableEvent {

  @Getter
  @Setter
  private Message message;

  @Getter
  private PlayerData player;

  public PlayerDeniedEvent(PlayerData player, Message message) {
    super();

    this.player = player;
    this.message = message;
  }
}
