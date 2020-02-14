package me.confuser.banmanager.sponge.listeners;


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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class ChatListener implements EventListener<MessageChannelEvent.Chat> {

  private final CommonChatListener listener;
  private BanManagerPlugin plugin;

  public ChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonChatListener(plugin);
  }

  public void onPlayerChat(MessageChannelEvent.Chat event, Player player) {
    CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.getUniqueId());

    if (listener.onPlayerChat(commonPlayer, new ChatHandler(event, player), event.getMessage().toPlain())) {
      event.setCancelled(true);
    }
  }

  public void onIpChat(MessageChannelEvent.Chat event, Player player) {
    CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.getUniqueId());

    if (listener.onIpChat(commonPlayer, player.getConnection().getAddress().getAddress(), new ChatHandler(event, player), event.getMessage().toPlain())) {
      event.setCancelled(true);
    }
  }

  @Override
  public void handle(MessageChannelEvent.Chat event) throws Exception {
    Optional<Player> firstPlayer = event.getCause().first(Player.class);

    if (!firstPlayer.isPresent()) return;

    onPlayerChat(event, firstPlayer.get());
    onIpChat(event, firstPlayer.get());
  }

  @RequiredArgsConstructor
  private class ChatHandler implements CommonChatHandler {
    private final MessageChannelEvent.Chat event;
    private final Player player;

    @Override
    public void handleSoftMute() {
      event.setChannel(player.getMessageChannel());
    }
  }
}
