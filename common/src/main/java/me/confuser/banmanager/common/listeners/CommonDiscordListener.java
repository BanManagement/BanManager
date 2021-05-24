package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.util.List;

public class CommonDiscordListener {
  private BanManagerPlugin plugin;

  public CommonDiscordListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public Object[] notifyOnBan(PlayerBanData ban) {
    String channelName;
    Message message;
    boolean ignoreSilent;

    if (ban.getExpires() == 0) {
      channelName = plugin.getDiscordConfig().getType("ban").getChannel();
      message = plugin.getDiscordConfig().getType("ban").getMessage();
      ignoreSilent = plugin.getDiscordConfig().getType("ban").isIgnoreSilent();
    } else {
      channelName = plugin.getDiscordConfig().getType("tempban").getChannel();
      message = plugin.getDiscordConfig().getType("tempban").getMessage();
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));

      ignoreSilent = plugin.getDiscordConfig().getType("tempban").isIgnoreSilent();
    }

    message.set("player", ban.getPlayer().getName())
        .set("playerId", ban.getPlayer().getUUID().toString())
        .set("actor", ban.getActor().getName())
        .set("reason", ban.getReason());

    return new Object[]{channelName, message, ignoreSilent};
  }

  public Object[] notifyOnBan(IpBanData ban) {
    List<PlayerData> players = plugin.getPlayerStorage().getDuplicates(ban.getIp());
    StringBuilder playerNames = new StringBuilder();

    for (PlayerData player : players) {
      playerNames.append(player.getName());
      playerNames.append(", ");
    }

    if (playerNames.length() >= 2) playerNames.setLength(playerNames.length() - 2);

    String channelName;
    Message message;
    boolean ignoreSilent;

    if (ban.getExpires() == 0) {
      channelName = plugin.getDiscordConfig().getType("banip").getChannel();
      message = plugin.getDiscordConfig().getType("banip").getMessage();
      ignoreSilent = plugin.getDiscordConfig().getType("banip").isIgnoreSilent();
    } else {
      channelName = plugin.getDiscordConfig().getType("tempbanip").getChannel();
      message = plugin.getDiscordConfig().getType("tempbanip").getMessage();
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
      ignoreSilent = plugin.getDiscordConfig().getType("tempbanip").isIgnoreSilent();
    }

    message.set("ip", ban.getIp().toString())
        .set("actor", ban.getActor().getName())
        .set("reason", ban.getReason())
        .set("players", playerNames.toString());

    return new Object[]{channelName, message, ignoreSilent};
  }

  public Object[] notifyOnKick(PlayerKickData kick) {
    String channelName = plugin.getDiscordConfig().getType("kick").getChannel();
    Message message = plugin.getDiscordConfig().getType("kick").getMessage();
    boolean ignoreSilent = plugin.getDiscordConfig().getType("kick").isIgnoreSilent();

    message.set("player", kick.getPlayer().getName())
        .set("playerId", kick.getPlayer().getUUID().toString())
        .set("actor", kick.getActor().getName())
        .set("reason", kick.getReason());

    return new Object[]{channelName, message, ignoreSilent};
  }

  public Object[] notifyOnMute(PlayerMuteData ban) {
    String channelName;
    Message message;
    boolean ignoreSilent;

    if (ban.getExpires() == 0) {
      channelName = plugin.getDiscordConfig().getType("mute").getChannel();
      message = plugin.getDiscordConfig().getType("mute").getMessage();
      ignoreSilent = plugin.getDiscordConfig().getType("mute").isIgnoreSilent();
    } else {
      channelName = plugin.getDiscordConfig().getType("tempmute").getChannel();
      message = plugin.getDiscordConfig().getType("tempmute").getMessage();
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));

      ignoreSilent = plugin.getDiscordConfig().getType("tempmute").isIgnoreSilent();
    }

    message.set("player", ban.getPlayer().getName())
        .set("playerId", ban.getPlayer().getUUID().toString())
        .set("actor", ban.getActor().getName())
        .set("reason", ban.getReason());

    return new Object[]{channelName, message, ignoreSilent};
  }

  public Object[] notifyOnWarn(PlayerWarnData ban) {
    String channelName = plugin.getDiscordConfig().getType("warning").getChannel();
    Message message = plugin.getDiscordConfig().getType("warning").getMessage();
    boolean ignoreSilent = plugin.getDiscordConfig().getType("warning").isIgnoreSilent();

    message.set("player", ban.getPlayer().getName())
        .set("playerId", ban.getPlayer().getUUID().toString())
        .set("actor", ban.getActor().getName())
        .set("reason", ban.getReason());

    return new Object[]{channelName, message, ignoreSilent};
  }

  public Object[] notifyOnUnban(PlayerBanData ban, PlayerData actor, String reason) {
    String channelName = plugin.getDiscordConfig().getType("unban").getChannel();
    Message message = plugin.getDiscordConfig().getType("unban").getMessage();

    message.set("player", ban.getPlayer().getName())
        .set("playerId", ban.getPlayer().getUUID().toString())
        .set("actor", actor.getName())
        .set("reason", reason);

    return new Object[]{channelName, message};
  }

  public Object[] notifyOnUnban(IpBanData ban, PlayerData actor, String reason) {
    String channelName = plugin.getDiscordConfig().getType("unbanip").getChannel();
    Message message = plugin.getDiscordConfig().getType("unbanip").getMessage();

    message.set("ip", ban.getIp().toString())
        .set("actor", actor.getName())
        .set("reason", reason);

    return new Object[]{channelName, message};
  }

  public Object[] notifyOnUnmute(PlayerMuteData mute, PlayerData actor, String reason) {
    String channelName = plugin.getDiscordConfig().getType("unmute").getChannel();
    Message message = plugin.getDiscordConfig().getType("unmute").getMessage();

    message.set("player", mute.getPlayer().getName())
        .set("playerId", mute.getPlayer().getUUID().toString())
        .set("actor", actor.getName())
        .set("reason", reason);

    return new Object[]{channelName, message};
  }
}
