package me.confuser.banmanager.fabric.listeners;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.fabric.FabricPlayer;
import me.confuser.banmanager.fabric.FabricServer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

public class JoinListener {
  private final CommonJoinListener listener;
  private BanManagerPlugin plugin;

  public JoinListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(plugin);

    ServerPlayConnectionEvents.INIT.register((handler, server) -> {
      BanJoinHandler banJoinHandler = new BanJoinHandler(plugin, handler);

      listener.banCheck(handler.getPlayer().getUuid(), handler.getPlayer().getName().getString(),
          IPUtils.toIPAddress(handler.getPlayer().getIp()), banJoinHandler);

      if (!banJoinHandler.isDenied()) {
        listener.onPreJoin(handler.getPlayer().getUuid(), handler.getPlayer().getName().getString(),
            IPUtils.toIPAddress(handler.getPlayer().getIp()));
      }
    });

    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
      listener.onJoin(
          new FabricPlayer(handler.getPlayer(), server, plugin.getConfig().isOnlineMode()));

      listener.onPlayerLogin(
          new FabricPlayer(handler.getPlayer(), server, plugin.getConfig().isOnlineMode()),
          new LoginHandler(handler.getPlayer()));
    });
  }

  @RequiredArgsConstructor
  private class BanJoinHandler implements CommonJoinHandler {
    private final BanManagerPlugin plugin;
    private final ServerPlayNetworkHandler handler;
    @Getter
    private boolean isDenied = false;

    @Override
    public void handlePlayerDeny(PlayerData player, Message message) {
      plugin.getServer().callEvent("PlayerDeniedEvent", player, message);

      handleDeny(message);
    }

    @Override
    public void handleDeny(Message message) {
      isDenied = true;

      handler.disconnect(FabricServer.formatMessage(message));
    }
  }

  @RequiredArgsConstructor
  private class LoginHandler implements CommonJoinHandler {
    private final ServerPlayerEntity player;

    @Override
    public void handlePlayerDeny(PlayerData player, Message message) {
      handleDeny(message);
    }

    @Override
    public void handleDeny(Message message) {
      player.networkHandler.disconnect(FabricServer.formatMessage(message));
    }
  }
}
