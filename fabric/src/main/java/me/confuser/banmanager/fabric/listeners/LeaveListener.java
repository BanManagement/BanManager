package me.confuser.banmanager.fabric.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonLeaveListener;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class LeaveListener {
  private final CommonLeaveListener listener;

  public LeaveListener(BanManagerPlugin plugin) {
    this.listener = new CommonLeaveListener(plugin);

    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
      ServerPlayerEntity player = handler.getPlayer();
      listener.onLeave(player.getUuid(), player.getName().getString());
    });
  }
}
