package me.confuser.banmanager.sponge.listeners;

import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import me.confuser.banmanager.common.BanManagerPlugin;
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

    send(listener.notifyOnBan(event.getBan()));
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnMute(PlayerMutedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnMute(event.getMute()));
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnWarn(event.getWarning()));
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnBan(IpBannedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnBan(event.getBan()));
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnKick(PlayerKickedEvent event) {
    if (event.isSilent()) return;

    send(listener.notifyOnKick(event.getKick()));
  }

  private void send(Object[] data) {
    FormatType formatType = FormatType.of(() -> data[1].toString());
    DiscordMessageBuilder
        .forChannel((String) data[0])
        .format(formatType)
        .useWebhook(false)
        .send();
  }
}
