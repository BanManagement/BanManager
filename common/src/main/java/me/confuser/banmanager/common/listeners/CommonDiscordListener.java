package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

public class CommonDiscordListener {
  private BanManagerPlugin plugin;

  public CommonDiscordListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public Object[] notifyOnBan(PlayerBanData ban) {
    String channelName;
    Message message;

    if (ban.getExpires() == 0) {
      channelName = plugin.getDiscordConfig().getType("ban").getChannel();
      message = plugin.getDiscordConfig().getType("ban").getMessage();
    } else {
      channelName = plugin.getDiscordConfig().getType("tempban").getChannel();
      message = plugin.getDiscordConfig().getType("tempban").getMessage();
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    message.set("player", ban.getPlayer().getName())
        .set("playerId", ban.getPlayer().getUUID().toString())
        .set("actor", ban.getActor().getName())
        .set("reason", ban.getReason());

    return new Object[]{channelName, message};
  }

  public Object[] notifyOnMute(PlayerMuteData ban) {
    String channelName;
    Message message;

    if (ban.getExpires() == 0) {
      channelName = plugin.getDiscordConfig().getType("mute").getChannel();
      message = plugin.getDiscordConfig().getType("mute").getMessage();
    } else {
      channelName = plugin.getDiscordConfig().getType("tempmute").getChannel();
      message = plugin.getDiscordConfig().getType("tempmute").getMessage();
      message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
    }

    message.set("player", ban.getPlayer().getName())
        .set("playerId", ban.getPlayer().getUUID().toString())
        .set("actor", ban.getActor().getName())
        .set("reason", ban.getReason());

    return new Object[]{channelName, message};
  }

  public Object[] notifyOnWarn(PlayerWarnData ban) {
    String channelName = plugin.getDiscordConfig().getType("warn").getChannel();
    Message message = plugin.getDiscordConfig().getType("warn").getMessage();

    message.set("player", ban.getPlayer().getName())
        .set("playerId", ban.getPlayer().getUUID().toString())
        .set("actor", ban.getActor().getName())
        .set("reason", ban.getReason());

    return new Object[]{channelName, message};
  }
}
