package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerData;


public class PluginReloadedEvent extends CustomCancellableEvent {

  @Getter
  private PlayerData actor;

  public PluginReloadedEvent(PlayerData actor) {
    super(false);

    this.actor = actor;
  }
}
