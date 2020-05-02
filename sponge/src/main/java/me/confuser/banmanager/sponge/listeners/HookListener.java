package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonHooksListener;
import me.confuser.banmanager.sponge.api.events.*;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

public class HookListener {
  private final CommonHooksListener listener;

  public HookListener(BanManagerPlugin plugin) {
    this.listener = new CommonHooksListener(plugin);
  }

  @Listener(order = Order.POST)
  public void onBan(final PlayerBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @Listener(order = Order.POST)
  public void onBan(final PlayerBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @Listener(order = Order.POST)
  public void onUnban(final PlayerUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @Listener(order = Order.POST)
  public void onMute(final PlayerMuteEvent event) {
    listener.onMute(event.getMute(), true);
  }

  @Listener(order = Order.POST)
  public void onMute(final PlayerMutedEvent event) {
    listener.onMute(event.getMute(), false);
  }

  @Listener(order = Order.POST)
  public void onUnmute(final PlayerUnmuteEvent event) {
    listener.onUnmute(event.getMute(), event.getActor(), event.getReason());
  }

  @Listener(order = Order.POST)
  public void onBan(final IpBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @Listener(order = Order.POST)
  public void onBan(final IpBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @Listener(order = Order.POST)
  public void onUnban(final IpUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @Listener(order = Order.POST)
  public void onBan(final IpRangeBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @Listener(order = Order.POST)
  public void onBan(final IpRangeBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @Listener(order = Order.POST)
  public void onUnban(final IpRangeUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @Listener(order = Order.POST)
  public void onWarn(final PlayerWarnEvent event) {
    listener.onWarn(event.getWarning(), true);
  }

  @Listener(order = Order.POST)
  public void onWarn(final PlayerWarnedEvent event) {
    listener.onWarn(event.getWarning(), false);
  }

  @Listener(order = Order.POST)
  public void onNote(final PlayerNoteCreatedEvent event) {
    listener.onNote(event.getNote());
  }

  @Listener(order = Order.POST)
  public void onReport(final PlayerReportEvent event) {
    listener.onReport(event.getReport(), true);
  }

  @Listener(order = Order.POST)
  public void onReport(final PlayerReportedEvent event) {
    listener.onReport(event.getReport(), false);
  }
}
