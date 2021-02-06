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
    if (event.isSilent()) return;

    Object[] data = listener.notifyOnBan(event.getBan());

    send(data, Bukkit.getPlayer(event.getBan().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnMute(PlayerMutedEvent event) {
    if (event.isSilent()) return;

    Object[] data = listener.notifyOnMute(event.getMute());

    send(data, Bukkit.getPlayer(event.getMute().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnBan(IpBannedEvent event) {
    if (event.isSilent()) return;

    Object[] data = listener.notifyOnBan(event.getBan());

    send(data, Bukkit.getPlayer(event.getBan().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnKick(PlayerKickedEvent event) {
    if (event.isSilent()) return;

    Object[] data = listener.notifyOnKick(event.getKick());

    send(data, Bukkit.getPlayer(event.getKick().getActor().getUUID()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    if (event.isSilent()) return;

    Object[] data = listener.notifyOnWarn(event.getWarning());

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

  private void send(Object[] data, Player actor) {
    if (actor == null) {
      DiscordUtil.sendMessage(DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName((String) data[0]), data[1].toString());
    } else {
      WebhookUtil.deliverMessage(DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName((String) data[0]), actor, data[1].toString());
    }
  }
}
