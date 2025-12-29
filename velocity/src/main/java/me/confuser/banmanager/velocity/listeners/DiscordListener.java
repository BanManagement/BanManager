package me.confuser.banmanager.velocity.listeners;

import me.confuser.banmanager.velocity.api.events.*;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonDiscordListener;
import com.velocitypowered.api.event.Subscribe;

import com.velocitypowered.api.event.PostOrder;

public class DiscordListener extends Listener {
  private CommonDiscordListener listener;

  public DiscordListener(BanManagerPlugin plugin) {
    this.listener = new CommonDiscordListener(plugin);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnBan(PlayerBannedEvent event) {
    Object[] data = listener.notifyOnBan(event.getBan());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnMute(PlayerMutedEvent event) {
    Object[] data = listener.notifyOnMute(event.getMute());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnBan(IpBannedEvent event) {
    Object[] data = listener.notifyOnBan(event.getBan());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnKick(PlayerKickedEvent event) {
    Object[] data = listener.notifyOnKick(event.getKick());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    Object[] data = listener.notifyOnWarn(event.getWarning());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnUnban(PlayerUnbanEvent event) {
    Object[] data = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());

    send(data);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnUnban(IpUnbanEvent event) {
    Object[] data = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());

    send(data);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnUnmute(PlayerUnmuteEvent event) {
    Object[] data = listener.notifyOnUnmute(event.getMute(), event.getActor(), event.getReason());

    send(data);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnReport(PlayerReportedEvent event) {
    Object[] data = listener.notifyOnReport(event.getReport(), event.getReport().getActor(), event.getReport().getReason());

    if (event.isSilent() && (boolean) data[2]) return;

    send(data);
  }

  private void send(Object[] data) {
    String url = (String) data[0];
    String payload = (String) data[1];

    if (url == null || payload == null || url.isEmpty() || payload.isEmpty()) return;

    listener.send(url, payload);
  }
}
