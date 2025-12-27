package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.data.PlayerData;


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
