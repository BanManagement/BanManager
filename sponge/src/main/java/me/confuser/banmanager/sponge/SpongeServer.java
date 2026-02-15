package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.util.ColorUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.sponge.api.events.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

public class SpongeServer implements CommonServer {
    private BanManagerPlugin plugin;
    private Server server;

    public void enable(BanManagerPlugin plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public CommonPlayer getPlayer(UUID uniqueId) {
        Optional<ServerPlayer> player = Sponge.server().player(uniqueId);
        return player.map(value -> new SpongePlayer(value, plugin.getConfig().isOnlineMode())).orElse(null);
    }

    @Override
    public CommonPlayer getPlayer(String name) {
        Optional<ServerPlayer> player = Sponge.server().player(name);
        return player.map(value -> new SpongePlayer(value, plugin.getConfig().isOnlineMode())).orElse(null);
    }

    @Override
    public CommonPlayer[] getOnlinePlayers() {
        return Sponge.server().onlinePlayers().stream()
            .map(player -> new SpongePlayer(player, plugin.getConfig().isOnlineMode()))
            .collect(Collectors.toList()).toArray(new CommonPlayer[0]);
    }

    @Override
    public void broadcast(String message, String permission) {
        if (message.isEmpty()) return;

        for (CommonPlayer player : getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }

        Sponge.systemSubject().sendMessage(formatMessage(message));
    }

    @Override
    public void broadcastJSON(TextComponent message, String permission) {
        Arrays.stream(getOnlinePlayers()).forEach(player -> {
            if (player.hasPermission(permission)) player.sendJSONMessage(message);
        });
    }

    public void broadcast(String message, String permission, CommonSender sender) {
        broadcast(message, permission);

        if (!sender.hasPermission(permission)) sender.sendMessage(message);
    }

    public static Component formatMessage(String message) {
        return LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build()
            .deserialize(ColorUtils.preprocess(message));
    }

    public static Component formatMessage(Message message) {
        return formatMessage(message.toString());
    }

    public static Component formatMessage(TextComponent message) {
        String json = GsonComponentSerializer.gson().serialize(message);
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(json);
    }

    public static Component formatJsonMessage(String message) {
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(message);
    }

    public CommonSender getConsoleSender() {
        return new SpongeSender(plugin, Sponge.systemSubject());
    }

    public boolean dispatchCommand(CommonSender sender, String command) {
        try {
            Sponge.server().commandManager().process(Sponge.systemSubject(), command);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Error dispatching command: " + e.getMessage());
            return false;
        }
    }

    public CommonWorld getWorld(String name) {
        for (ServerWorld world : Sponge.server().worldManager().worlds()) {
            if (world.key().asString().equals(name)) {
                return new CommonWorld(name);
            }
        }
        return null;
    }

    @Override
    public CommonEvent callEvent(String name, Object... args) {
        CustomEvent event = null;
        CommonEvent commonEvent = new CommonEvent(false, true);

        switch (name) {
            case "PlayerBanEvent":
                event = new PlayerBanEvent((PlayerBanData) args[0], (boolean) args[1]);
                break;
            case "PlayerBannedEvent":
                event = new PlayerBannedEvent((PlayerBanData) args[0], (boolean) args[1]);
                break;
            case "PlayerUnbanEvent":
                event = new PlayerUnbanEvent((PlayerBanData) args[0], (PlayerData) args[1], (String) args[2], (boolean) args[3]);
                break;

            case "IpBanEvent":
                event = new IpBanEvent((IpBanData) args[0], (boolean) args[1]);
                break;
            case "IpBannedEvent":
                event = new IpBannedEvent((IpBanData) args[0], (boolean) args[1]);
                break;
            case "IpUnbanEvent":
                event = new IpUnbanEvent((IpBanData) args[0], (PlayerData) args[1], (String) args[2], (boolean) args[3]);
                break;

            case "IpMuteEvent":
                event = new IpMuteEvent((IpMuteData) args[0], (boolean) args[1]);
                break;
            case "IpMutedEvent":
                event = new IpMutedEvent((IpMuteData) args[0], (boolean) args[1]);
                break;
            case "IpUnmutedEvent":
                event = new IpUnmutedEvent((IpMuteData) args[0], (PlayerData) args[1], (String) args[2], (boolean) args[3]);
                break;

            case "PlayerKickedEvent":
                event = new PlayerKickedEvent((PlayerKickData) args[0], (boolean) args[1]);
                break;

            case "PlayerNoteCreatedEvent":
                event = new PlayerNoteCreatedEvent((PlayerNoteData) args[0], args.length > 1 && (boolean) args[1]);
                break;

            case "PlayerReportEvent":
                event = new PlayerReportEvent((PlayerReportData) args[0], (boolean) args[1]);
                break;
            case "PlayerReportedEvent":
                event = new PlayerReportedEvent((PlayerReportData) args[0], (boolean) args[1]);
                break;
            case "PlayerReportDeletedEvent":
                event = new PlayerReportDeletedEvent((PlayerReportData) args[0]);
                break;

            case "NameBanEvent":
                event = new NameBanEvent((NameBanData) args[0], (boolean) args[1]);
                break;
            case "NameBannedEvent":
                event = new NameBannedEvent((NameBanData) args[0], (boolean) args[1]);
                break;
            case "NameUnbanEvent":
                event = new NameUnbanEvent((NameBanData) args[0], (PlayerData) args[1], (String) args[2], (boolean) args[3]);
                break;

            case "PlayerWarnEvent":
                event = new PlayerWarnEvent((PlayerWarnData) args[0], (boolean) args[1]);
                break;
            case "PlayerWarnedEvent":
                event = new PlayerWarnedEvent((PlayerWarnData) args[0], (boolean) args[1]);
                break;

            case "IpRangeBanEvent":
                event = new IpRangeBanEvent((IpRangeBanData) args[0], (boolean) args[1]);
                break;
            case "IpRangeBannedEvent":
                event = new IpRangeBannedEvent((IpRangeBanData) args[0], (boolean) args[1]);
                break;
            case "IpRangeUnbanEvent":
                event = new IpRangeUnbanEvent((IpRangeBanData) args[0], (PlayerData) args[1], (String) args[2], (boolean) args[3]);
                break;

            case "PlayerMuteEvent":
                event = new PlayerMuteEvent((PlayerMuteData) args[0], (boolean) args[1]);
                break;
            case "PlayerMutedEvent":
                event = new PlayerMutedEvent((PlayerMuteData) args[0], (boolean) args[1]);
                break;
            case "PlayerUnmuteEvent":
                event = new PlayerUnmuteEvent((PlayerMuteData) args[0], (PlayerData) args[1], (String) args[2], (boolean) args[3]);
                break;

            case "PluginReloadedEvent":
                event = new PluginReloadedEvent((PlayerData) args[0]);
                break;

            case "PlayerDeniedEvent":
                event = new PlayerDeniedEvent((PlayerData) args[0], (Message) args[1]);
                break;
        }

        if (event == null) {
            plugin.getLogger().warning("Unable to call missing event " + name);
            return commonEvent;
        }

        Sponge.eventManager().post(event);

        if (event instanceof SilentCancellableEvent) {
            commonEvent = new CommonEvent(((SilentCancellableEvent) event).isCancelled(), ((SilentCancellableEvent) event).isSilent());
        } else if (event instanceof SilentEvent) {
            commonEvent = new CommonEvent(false, ((SilentEvent) event).isSilent());
        } else if (event instanceof CustomCancellableEvent) {
            commonEvent = new CommonEvent(((CustomCancellableEvent) event).isCancelled(), true);
        }

        return commonEvent;
    }

    @Override
    public CommonExternalCommand getPluginCommand(String commandName) {
        Optional<CommandMapping> commandMapping = Sponge.server().commandManager().commandMapping(commandName);

        if (commandMapping.isEmpty()) return null;

        CommandMapping cmd = commandMapping.get();

        return new CommonExternalCommand(null, cmd.primaryAlias(), new ArrayList<>(cmd.allAliases()));
    }
}
