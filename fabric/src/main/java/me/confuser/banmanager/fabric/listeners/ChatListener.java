package me.confuser.banmanager.fabric.listeners;


import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonChatHandler;
import me.confuser.banmanager.common.listeners.CommonChatListener;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.message.MessageType.Parameters;
import net.minecraft.server.network.ServerPlayerEntity;

public class ChatListener {
  private final CommonChatListener listener;
  private BanManagerPlugin plugin;

  public ChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonChatListener(plugin);

    ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
      if (!sender.isPlayer()) {
        return true;
      }

      CommonPlayer player = plugin.getServer().getPlayer(sender.getUuid());

      if (listener.onPlayerChat(player, new ChatHandler(message, sender, params), message.getSignedContent())) {
        return false;
      }

      if (listener.onIpChat(player, player.getAddress(), new ChatHandler(message, sender, params), message.getSignedContent())) {
        return false;
      }

      return true;
    });
  }

  @RequiredArgsConstructor
  private class ChatHandler implements CommonChatHandler {
    private final SignedMessage message;
    private final ServerPlayerEntity sender;
    private final Parameters params;

    @Override
    public void handleSoftMute() {
      plugin.getLogger().warning("Soft mute not implemented");
    }
  }
}
