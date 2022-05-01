package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerData;


public class PluginReloadedEvent extends CustomCancellableEvent {

  @Getter
  private PlayerData actor;

  public PluginReloadedEvent(PlayerData actor) {
    super();

    this.actor = actor;
  }
}
