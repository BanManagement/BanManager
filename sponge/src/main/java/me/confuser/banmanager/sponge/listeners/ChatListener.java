package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerWarnData;
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

  private BanManagerPlugin plugin;

  public ChatListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void onPlayerChat(MessageChannelEvent.Chat event, Player player) {
    UUID uuid = player.getUniqueId();
    CommonPlayer commonPlayer = plugin.getServer().getPlayer(uuid);

    if (!plugin.getPlayerMuteStorage().isMuted(uuid)) {
      if (plugin.getPlayerWarnStorage().isMuted(uuid)) {
        PlayerWarnData warning = plugin.getPlayerWarnStorage().getMute(uuid);

        if (warning.getReason().toLowerCase().equals(event.getMessage().toPlain())) {
          plugin.getPlayerWarnStorage().removeMute(uuid);
          Message.get("warn.player.disallowed.removed").sendTo(commonPlayer);
        } else {
          Message.get("warn.player.disallowed.header").sendTo(commonPlayer);
          Message.get("warn.player.disallowed.reason").set("reason", warning.getReason()).sendTo(commonPlayer);
        }

        event.setCancelled(true);
      }

      return;
    }

    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(uuid);

    if (mute.hasExpired()) {
      try {
        plugin.getPlayerMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return;
    }

    if (mute.isSoft()) {
      event.setChannel(player.getMessageChannel());

      return;
    }

    event.setCancelled(true);

    Message broadcast = Message.get("mute.player.broadcast")
        .set("message", event.getMessage().toString())
        .set("displayName", commonPlayer.getDisplayName())
        .set("player", commonPlayer.getName())
        .set("playerId", uuid.toString())
        .set("reason", mute.getReason())
        .set("actor", mute.getActor().getName());

    plugin.getServer().broadcast(broadcast.toString(), "bm.notify.muted");

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("mute.player.disallowed");
    } else {
      message = Message.get("tempmute.player.disallowed")
          .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", commonPlayer.getDisplayName())
        .set("player", commonPlayer.getName())
        .set("playerId", uuid.toString())
        .set("reason", mute.getReason())
        .set("actor", mute.getActor().getName());

    commonPlayer.sendMessage(message.toString());
  }

  public void onIpChat(MessageChannelEvent.Chat event, Player player) {
    if (!plugin.getIpMuteStorage().isMuted(player.getConnection().getAddress().getAddress())) {
      return;
    }

    IpMuteData mute = plugin.getIpMuteStorage().getMute(player.getConnection().getAddress().getAddress());
    CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.getUniqueId());

    if (mute.hasExpired()) {
      try {
        plugin.getIpMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return;
    }

    if (mute.isSoft()) {
      event.setChannel(player.getMessageChannel());

      return;
    }

    event.setCancelled(true);

    Message broadcast = Message.get("muteip.ip.broadcast")
        .set("message", event.getMessage().toString())
        .set("displayName", commonPlayer.getDisplayName())
        .set("player", commonPlayer.getName())
        .set("playerId", commonPlayer.getUniqueId().toString())
        .set("reason", mute.getReason())
        .set("actor", mute.getActor().getName());

    plugin.getServer().broadcast(broadcast.toString(), "bm.notify.mutedip");

    Message message;

    if (mute.getExpires() == 0) {
      message = Message.get("muteip.ip.disallowed");
    } else {
      message = Message.get("tempmuteip.ip.disallowed")
          .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
    }

    message.set("displayName", commonPlayer.getDisplayName())
        .set("player", commonPlayer.getName())
        .set("playerId", commonPlayer.getUniqueId().toString())
        .set("reason", mute.getReason())
        .set("actor", mute.getActor().getName())
        .set("ip", mute.getIp().toString());

    message.sendTo(commonPlayer);
  }

  @Override
  public void handle(MessageChannelEvent.Chat event) throws Exception {
    Optional<Player> firstPlayer = event.getCause().first(Player.class);

    if (!firstPlayer.isPresent()) return;

    onPlayerChat(event, firstPlayer.get());
    onIpChat(event, firstPlayer.get());
  }
}
