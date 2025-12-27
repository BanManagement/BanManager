package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;

import java.sql.SQLException;

public class SpongeSender implements CommonSender {

    private final BanManagerPlugin plugin;
    private final Audience audience;
    private final Subject subject;
    private final CommandCause commandCause;

    public SpongeSender(BanManagerPlugin plugin, Audience audience) {
        this.plugin = plugin;
        this.audience = audience;
        this.commandCause = null;
        this.subject = (audience instanceof Subject) ? (Subject) audience : null;
    }

    public SpongeSender(BanManagerPlugin plugin, CommandCause cause) {
        this.plugin = plugin;
        this.commandCause = cause;
        this.audience = cause.audience();
        this.subject = cause.subject();
    }

    @Override
    public String getName() {
        if (audience instanceof ServerPlayer) {
            return ((ServerPlayer) audience).name();
        }
        if (commandCause != null) {
            Object root = commandCause.root();
            if (root instanceof ServerPlayer) {
                return ((ServerPlayer) root).name();
            }
        }
        return "Console";
    }

    @Override
    public boolean hasPermission(String permission) {
        if (subject != null) {
            return subject.hasPermission(permission);
        }
        // Console has all permissions
        return isConsole();
    }

    @Override
    public void sendMessage(String message) {
        audience.sendMessage(SpongeServer.formatMessage(message));
    }

    @Override
    public void sendMessage(Message message) {
        sendMessage(message.toString());
    }

    @Override
    public boolean isConsole() {
        if (audience instanceof ServerPlayer) {
            return false;
        }
        if (commandCause != null) {
            return !(commandCause.root() instanceof ServerPlayer);
        }
        return true;
    }

    @Override
    public PlayerData getData() {
        if (isConsole()) {
            return plugin.getPlayerStorage().getConsole();
        }

        ServerPlayer player = null;
        if (audience instanceof ServerPlayer) {
            player = (ServerPlayer) audience;
        } else if (commandCause != null) {
            Object root = commandCause.root();
            if (root instanceof ServerPlayer) {
                player = (ServerPlayer) root;
            }
        }

        if (player != null) {
            try {
                return plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player.uniqueId()));
            } catch (SQLException e) {
                e.printStackTrace();
                sendMessage(Message.get("sender.error.exception").toString());
                return null;
            }
        }

        return null;
    }
}
