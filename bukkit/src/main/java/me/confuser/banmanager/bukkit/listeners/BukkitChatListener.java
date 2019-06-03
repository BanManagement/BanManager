package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.utils.BukkitCommandUtils;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.util.BukkitUUIDUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.UUID;

public class BukkitChatListener implements Listener {

    private final BMBukkitPlugin plugin;

    public BukkitChatListener(BMBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID uuid = BukkitUUIDUtils.getUUID(event.getPlayer());

        if (!plugin.getPlayerMuteStorage().isMuted(uuid)) {
            if (plugin.getPlayerWarnStorage().isMuted(uuid)) {
                PlayerWarnData warning = plugin.getPlayerWarnStorage().getMute(uuid);

                if (warning.getReason().toLowerCase().equals(event.getMessage().toLowerCase())) {
                    plugin.getPlayerWarnStorage().removeMute(uuid);
                    Message.WARN_PLAYER_DISALLOWED_REMOVED.send((Sender) event.getPlayer());
                } else {
                    Message.WARN_PLAYER_DISALLOWED_HEADER.send((Sender) event.getPlayer());
                    Message.WARN_PLAYER_DISALLOWED_REASON.send((Sender) event.getPlayer(), "reason", warning.getReason());
                }

                event.setCancelled(true);
            }

            return;
        }

        PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(uuid);

        if (mute.hasExpired()) {
            try {
                plugin.getPlayerMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        if (mute.isSoft()) {
            event.getRecipients().clear();
            event.getRecipients().add(event.getPlayer());
            return;
        }

        event.setCancelled(true);

        String broadcast = Message.MUTE_PLAYER_BROADCAST.asString(plugin.getLocaleManager(),
                "message", event.getMessage(),
                "displayName", event.getPlayer().getDisplayName(),
                "player", event.getPlayer().getName(),
                "playerId", uuid.toString(),
                "reason", mute.getReason(),
                "actor", mute.getActor().getName());

        BukkitCommandUtils.broadcast(broadcast, "bm.notify.muted");

        Message message;

        if (mute.getExpires() == 0) {
            message = Message.MUTE_PLAYER_DISALLOWED;
        } else {
            message = Message.TEMPMUTE_PLAYER_DISALLOWED;
        }

        message.send((Sender) event.getPlayer(),
                "displayName", event.getPlayer().getDisplayName(),
                "player", event.getPlayer().getName(),
                "playerId", uuid.toString(),
                "reason", mute.getReason(),
                "actor", mute.getActor().getName(),
                "expires", DateUtils.getDifferenceFormat(mute.getExpires()));

    }

    public void onIpChat(AsyncPlayerChatEvent event) {
        if (!plugin.getIpMuteStorage().isMuted(event.getPlayer().getAddress().getAddress())) {
            return;
        }

        IpMuteData mute = plugin.getIpMuteStorage().getMute(event.getPlayer().getAddress().getAddress());

        if (mute.hasExpired()) {
            try {
                plugin.getIpMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        if (mute.isSoft()) {
            event.getRecipients().clear();
            event.getRecipients().add(event.getPlayer());
            return;
        }

        event.setCancelled(true);

        String broadcast = Message.MUTEIP_IP_BROADCAST.asString(plugin.getLocaleManager(),
                "message", event.getMessage(),
                "displayName", event.getPlayer().getDisplayName(),
                "player", event.getPlayer().getName(),
                "playerId", BukkitUUIDUtils.getUUID(event.getPlayer()).toString(),
                "reason", mute.getReason(),
                "actor", mute.getActor().getName());

        BukkitCommandUtils.broadcast(broadcast, "bm.notify.mutedip");

        Message message;

        if (mute.getExpires() == 0) {
            message = Message.MUTEIP_IP_DISALLOWED;
        } else {
            message = Message.TEMPMUTEIP_IP_DISALLOWED;
        }

        message.send((Sender) event.getPlayer(),
                "displayName", event.getPlayer().getDisplayName(),
                "player", event.getPlayer().getName(),
                "playerId", BukkitUUIDUtils.getUUID(event.getPlayer()).toString(),
                "reason", mute.getReason(),
                "actor", mute.getActor().getName(),
                "ip", IPUtils.toString(mute.getIp()),
                "expires", DateUtils.getDifferenceFormat(mute.getExpires()));

        event.getPlayer().sendMessage(message.toString());
    }
}
