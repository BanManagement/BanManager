package me.confuser.banmanager.bungee.listeners;


import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonChatHandler;
import me.confuser.banmanager.common.listeners.CommonChatListener;
import me.confuser.banmanager.common.listeners.CommonCommandListener;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {
  private final CommonChatListener listener;
  private final CommonCommandListener commandListener;
  private BanManagerPlugin plugin;

  public ChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonChatListener(plugin);
    this.commandListener = new CommonCommandListener(plugin);
  }

  @EventHandler
  public void onPlayerChat(ChatEvent event) {
    if (!(event.getSender() instanceof ProxiedPlayer)) return;

    CommonPlayer player = plugin.getServer().getPlayer(((ProxiedPlayer) event.getSender()).getUniqueId());

    if (event.isCommand()) {
      // Split the command
      String[] args = event.getMessage().split(" ", 6);
      // Get rid of the first /
      String cmd = args[0].replace("/", "").toLowerCase();

      if (commandListener.onCommand(player, cmd, args)) {
        event.setCancelled(true);
      }
    } else if (listener.onPlayerChat(player, new ChatHandler(event), event.getMessage())) {
      event.setCancelled(true);
    } else if (listener.onIpChat(player, event.getSender().getAddress().getAddress(), new ChatHandler(event), event.getMessage())) {
      event.setCancelled(true);
    }
  }

  @RequiredArgsConstructor
  private class ChatHandler implements CommonChatHandler {
    private final ChatEvent event;

    @Override
    public void handleSoftMute() {
      event.setCancelled(true);
      ((ProxiedPlayer) event.getSender()).sendMessage(event.getMessage());
    }
  }
}
