package me.confuser.banmanager.velocity.listeners;


import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonChatHandler;
import me.confuser.banmanager.common.listeners.CommonChatListener;
import me.confuser.banmanager.common.listeners.CommonCommandListener;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.VelocityServer;
import net.kyori.adventure.text.Component;

public class ChatListener extends Listener {
  private final CommonChatListener listener;
  private final BanManagerPlugin plugin;

  public ChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;

    this.listener = new CommonChatListener(plugin);
    new CommonCommandListener(plugin);
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
      event.setResult(PlayerChatEvent.ChatResult.denied());
      event.getPlayer().sendMessage((Component) VelocityServer.formatMessage(event.getMessage()));
    }
  }
}
