package me.confuser.banmanager.bungee.listeners;

import me.confuser.banmanager.bungee.api.events.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonHooksListener;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class HookListener implements Listener {
  private final CommonHooksListener listener;

  public HookListener(BanManagerPlugin plugin) {
    this.listener = new CommonHooksListener(plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBan(final PlayerBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBan(final PlayerBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onUnban(final PlayerUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMute(final PlayerMuteEvent event) {
    listener.onMute(event.getMute(), true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMute(final PlayerMutedEvent event) {
    listener.onMute(event.getMute(), false);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onUnmute(final PlayerUnmuteEvent event) {
    listener.onUnmute(event.getMute(), event.getActor(), event.getReason());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBan(final IpBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBan(final IpBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onUnban(final IpUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBan(final IpRangeBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBan(final IpRangeBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onUnban(final IpRangeUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onWarn(final PlayerWarnEvent event) {
    listener.onWarn(event.getWarning(), true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onWarn(final PlayerWarnedEvent event) {
    listener.onWarn(event.getWarning(), false);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onNote(final PlayerNoteCreatedEvent event) {
    listener.onNote(event.getNote());
  }
}
