package me.confuser.banmanager.sponge.listeners;

import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.listeners.CommonDiscordListener;
import me.confuser.banmanager.sponge.api.events.*;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

public class DiscordListener {
  private CommonDiscordListener listener;

  public DiscordListener(BanManagerPlugin plugin) {
    this.listener = new CommonDiscordListener(plugin);
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnBan(PlayerBannedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnBan(event.getBan()), event.getBan().getActor());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnMute(PlayerMutedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnMute(event.getMute()), event.getMute().getActor());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnWarn(event.getWarning()), event.getWarning().getActor());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnBan(IpBannedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnBan(event.getBan()), event.getBan().getActor());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnKick(PlayerKickedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnKick(event.getKick()), event.getKick().getActor());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnUnban(PlayerUnbanEvent event) {
    send(listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason()), event.getActor());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnUnban(IpUnbanEvent event) {
    send(listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason()), event.getActor());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnUnmute(PlayerUnmuteEvent event) {
    send(listener.notifyOnUnmute(event.getMute(), event.getActor(), event.getReason()), event.getActor());
  }

  private void send(Object[] data, PlayerData actor) {
    DiscordMessageBuilder
        .forChannel((String) data[0])
        .placeholder("message", data[1].toString())
        .placeholder("player", actor.getName())
        .placeholder("uuid", actor.getUUID().toString())
        .placeholder("prefix", "")
        .format(FormatType.SERVER_TO_DISCORD_FORMAT)
        .send();
  }
}
