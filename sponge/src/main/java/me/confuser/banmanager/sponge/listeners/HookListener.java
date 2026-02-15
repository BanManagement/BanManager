package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonHooksListener;
import me.confuser.banmanager.sponge.api.events.*;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

/**
 * Listens for BanManager custom events and triggers configured hooks.
 */
public class HookListener {
    private final CommonHooksListener listener;

    public HookListener(BanManagerPlugin plugin) {
        this.listener = new CommonHooksListener(plugin);
    }

    @Listener(order = Order.POST)
    public void onBan(final PlayerBanEvent event) {
        listener.onBan(event.getBan(), true, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onBanned(final PlayerBannedEvent event) {
        listener.onBan(event.getBan(), false, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onUnban(final PlayerUnbanEvent event) {
        listener.onUnban(event.getBan(), event.getActor(), event.getReason(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onMute(final PlayerMuteEvent event) {
        listener.onMute(event.getMute(), true, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onMuted(final PlayerMutedEvent event) {
        listener.onMute(event.getMute(), false, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onUnmute(final PlayerUnmuteEvent event) {
        listener.onUnmute(event.getMute(), event.getActor(), event.getReason(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onIpBan(final IpBanEvent event) {
        listener.onBan(event.getBan(), true, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onIpBanned(final IpBannedEvent event) {
        listener.onBan(event.getBan(), false, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onIpUnban(final IpUnbanEvent event) {
        listener.onUnban(event.getBan(), event.getActor(), event.getReason(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onIpRangeBan(final IpRangeBanEvent event) {
        listener.onBan(event.getBan(), true, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onIpRangeBanned(final IpRangeBannedEvent event) {
        listener.onBan(event.getBan(), false, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onIpRangeUnban(final IpRangeUnbanEvent event) {
        listener.onUnban(event.getBan(), event.getActor(), event.getReason(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onWarn(final PlayerWarnEvent event) {
        listener.onWarn(event.getWarning(), true, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onWarned(final PlayerWarnedEvent event) {
        listener.onWarn(event.getWarning(), false, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onNote(final PlayerNoteCreatedEvent event) {
        listener.onNote(event.getNote(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onReport(final PlayerReportEvent event) {
        listener.onReport(event.getReport(), true, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void onReported(final PlayerReportedEvent event) {
        listener.onReport(event.getReport(), false, event.isSilent());
    }
}
