package me.confuser.banmanager.bukkit.listeners;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import github.scarsz.discordsrv.util.WebhookUtil;
import me.confuser.banmanager.bukkit.api.events.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonDiscordListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DiscordListener implements Listener {
  private BanManagerPlugin plugin;
  private CommonDiscordListener listener;

  public DiscordListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonDiscordListener(plugin);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnBan(PlayerBannedEvent event) {
    Object[] data = listener.notifyOnBan(event.getBan());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data, Bukkit.getPlayer(event.getBan().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnMute(PlayerMutedEvent event) {
    Object[] data = listener.notifyOnMute(event.getMute());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data, Bukkit.getPlayer(event.getMute().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnBan(IpBannedEvent event) {
    Object[] data = listener.notifyOnBan(event.getBan());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data, Bukkit.getPlayer(event.getBan().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnKick(PlayerKickedEvent event) {
    Object[] data = listener.notifyOnKick(event.getKick());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data, Bukkit.getPlayer(event.getKick().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    Object[] data = listener.notifyOnWarn(event.getWarning());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data, Bukkit.getPlayer(event.getWarning().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnUnban(PlayerUnbanEvent event) {
    Object[] data = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());

    send(data, Bukkit.getPlayer(event.getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnUnban(IpUnbanEvent event) {
    Object[] data = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());

    send(data, Bukkit.getPlayer(event.getBan().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnUnmute(PlayerUnmuteEvent event) {
    Object[] data = listener.notifyOnUnmute(event.getMute(), event.getActor(), event.getReason());

    send(data, Bukkit.getPlayer(event.getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnReport(PlayerReportedEvent event) {
    Object[] data = listener.notifyOnReport(event.getReport(), event.getReport().getActor(), event.getReport().getReason());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data, Bukkit.getPlayer(event.getReport().getActor().getUUID()));
  }

  private void send(Object[] data, Player actor) {
    if (actor == null || !plugin.getDiscordConfig().isMessagesFromActor()) {
      DiscordUtil.sendMessage(DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName((String) data[0]), data[1].toString());
    } else {
      WebhookUtil.deliverMessage(DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName((String) data[0]), actor, data[1].toString());
    }
  }
}
