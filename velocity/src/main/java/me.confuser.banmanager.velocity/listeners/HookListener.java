package me.confuser.banmanager.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;

import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.api.events.*;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonHooksListener;

public class HookListener extends Listener {
  private final CommonHooksListener listener;

  public HookListener(BanManagerPlugin plugin) {
    this.listener = new CommonHooksListener(plugin);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onBan(final PlayerBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onBan(final PlayerBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onUnban(final PlayerUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onMute(final PlayerMuteEvent event) {
    listener.onMute(event.getMute(), true);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onMute(final PlayerMutedEvent event) {
    listener.onMute(event.getMute(), false);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onUnmute(final PlayerUnmuteEvent event) {
    listener.onUnmute(event.getMute(), event.getActor(), event.getReason());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onBan(final IpBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onBan(final IpBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onUnban(final IpUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onBan(final IpRangeBanEvent event) {
    listener.onBan(event.getBan(), true);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onBan(final IpRangeBannedEvent event) {
    listener.onBan(event.getBan(), false);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onUnban(final IpRangeUnbanEvent event) {
    listener.onUnban(event.getBan(), event.getActor(), event.getReason());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onWarn(final PlayerWarnEvent event) {
    listener.onWarn(event.getWarning(), true);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onWarn(final PlayerWarnedEvent event) {
    listener.onWarn(event.getWarning(), false);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onNote(final PlayerNoteCreatedEvent event) {
    listener.onNote(event.getNote());
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onReport(final PlayerReportEvent event) {
    listener.onReport(event.getReport(), true);
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onReport(final PlayerReportedEvent event) {
    listener.onReport(event.getReport(), false);
  }
}
