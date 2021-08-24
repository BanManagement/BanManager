package me.confuser.banmanager.bukkit.listeners;


import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonChatHandler;
import me.confuser.banmanager.common.listeners.CommonChatListener;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
