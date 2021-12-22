package me.confuser.banmanager.velocity.listeners;


import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;

import lombok.RequiredArgsConstructor;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonChatHandler;
import me.confuser.banmanager.common.listeners.CommonChatListener;
import me.confuser.banmanager.common.listeners.CommonCommandListener;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.VelocityServer;

public class ChatListener extends Listener {
  private final CommonChatListener listener;
  private final CommonCommandListener commandListener;
  private BanManagerPlugin plugin;

  public ChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonChatListener(plugin);
    this.commandListener = new CommonCommandListener(plugin);
  }

  @Subscribe(order = PostOrder.LATE)
  public void onPlayerChat(PlayerChatEvent event) {
    CommonPlayer player = plugin.getServer().getPlayer(event.getPlayer().getUniqueId());

     if (listener.onPlayerChat(player, new ChatHandler(event), event.getMessage())) {
       event.setResult(PlayerChatEvent.ChatResult.denied());
    } else if (listener.onIpChat(player, event.getPlayer().getRemoteAddress().getAddress(), new ChatHandler(event), event.getMessage())) {
       event.setResult(PlayerChatEvent.ChatResult.denied());
    }
  }

  @RequiredArgsConstructor
  private class ChatHandler implements CommonChatHandler {
    private final PlayerChatEvent event;
    @Override
    public void handleSoftMute() {
      event.setResult(PlayerChatEvent.ChatResult.denied()); // Should be eqivalent to the chat being cancelled...
      // @TODO this needs to be passed to a legacy serializer
      event.getPlayer().sendMessage(VelocityServer.formatMessage(event.getMessage()));
    }
  }
}
