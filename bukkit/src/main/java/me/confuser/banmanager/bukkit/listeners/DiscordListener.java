package me.confuser.banmanager.bukkit.listeners;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.confuser.banmanager.bukkit.api.events.PlayerBannedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerMutedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerWarnedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DiscordListener implements Listener {
  private BanManagerPlugin plugin;

  public DiscordListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnBan(PlayerBannedEvent event) {
    if (event.isSilent()) return;

    PlayerBanData ban = event.getBan();

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

    DiscordUtil.sendMessage(DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName), message.toString());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnMute(PlayerMutedEvent event) {
    if (event.isSilent()) return;

    PlayerMuteData ban = event.getMute();

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

    DiscordUtil.sendMessage(DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName), message.toString());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    if (event.isSilent()) return;

    PlayerWarnData ban = event.getWarning();

    String channelName = plugin.getDiscordConfig().getType("warn").getChannel();
    Message message = plugin.getDiscordConfig().getType("warn").getMessage();

    message.set("player", ban.getPlayer().getName())
        .set("playerId", ban.getPlayer().getUUID().toString())
        .set("actor", ban.getActor().getName())
        .set("reason", ban.getReason());

    DiscordUtil.sendMessage(DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName), message.toString());
  }
}
