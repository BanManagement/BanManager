package me.confuser.banmanager.fabric.listeners;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonChatHandler;
import me.confuser.banmanager.common.listeners.CommonChatListener;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class ChatListener {
  private final CommonChatListener listener;

  public ChatListener(BanManagerPlugin plugin) {
    this.listener = new CommonChatListener(plugin);

    ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
      if (!sender.isPlayer()) {
        return true;
      }

      CommonPlayer player = plugin.getServer().getPlayer(sender.getUuid());
      ChatHandler handler = new ChatHandler();
      boolean cancelled = listener.onPlayerChat(player, handler, message.getSignedContent()) || listener.onIpChat(player, player.getAddress(), handler, message.getSignedContent());

      if (handler.isSoftMuted()) {
        sender.sendMessageToClient(params.applyChatDecoration(message.getContent()), false);
        return false;
      }

      return cancelled ? false : true;
    });
  }

  @RequiredArgsConstructor
  private class ChatHandler implements CommonChatHandler {
    @Getter
    private boolean isSoftMuted = false;

    @Override
    public void handleSoftMute() {
      isSoftMuted = true;
    }
  }
}
