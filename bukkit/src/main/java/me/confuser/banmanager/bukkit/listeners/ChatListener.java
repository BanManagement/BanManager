package me.confuser.banmanager.bukkit.listeners;


import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.listeners.CommonChatHandler;
import me.confuser.banmanager.common.listeners.CommonChatListener;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.UUID;

public class ChatListener implements Listener {
  private final CommonChatListener listener;
  private BanManagerPlugin plugin;

  public ChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonChatListener(plugin);
  }

  public void onPlayerChat(AsyncPlayerChatEvent event) {
    CommonPlayer player = plugin.getServer().getPlayer(event.getPlayer().getUniqueId());

    if (listener.onPlayerChat(player, new ChatHandler(event), event.getMessage())) {
      event.setCancelled(true);
    }
  }

  public void onIpChat(AsyncPlayerChatEvent event) {
    CommonPlayer player = plugin.getServer().getPlayer(event.getPlayer().getUniqueId());

    if (listener.onIpChat(player, event.getPlayer().getAddress().getAddress(), new ChatHandler(event), event.getMessage())) {
      event.setCancelled(true);
    }
  }

  @RequiredArgsConstructor
  private class ChatHandler implements CommonChatHandler {
    private final AsyncPlayerChatEvent event;

    @Override
    public void handleSoftMute() {
      event.getRecipients().clear();
      event.getRecipients().add(event.getPlayer());
    }
  }
}
