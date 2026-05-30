package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.util.ColorUtils;
import me.confuser.banmanager.common.util.Message;
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
        return player.map(value -> new SpongePlayer(plugin, value, plugin.getConfig().isOnlineMode())).orElse(null);
    }

    @Override
    public CommonPlayer getPlayer(String name) {
        Optional<ServerPlayer> player = Sponge.server().player(name);
        return player.map(value -> new SpongePlayer(plugin, value, plugin.getConfig().isOnlineMode())).orElse(null);
    }

    @Override
    public CommonPlayer getPlayerExact(String name) {
        return getPlayer(name);
    }

    @Override
    public CommonPlayer[] getOnlinePlayers() {
        return Sponge.server().onlinePlayers().stream()
            .map(player -> new SpongePlayer(plugin, player, plugin.getConfig().isOnlineMode()))
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
        return formatMessage(message.resolveComponent());
    }

    public static Component formatMessage(me.confuser.banmanager.common.kyori.text.Component message) {
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
    public CommonExternalCommand getPluginCommand(String commandName) {
        Optional<CommandMapping> commandMapping = Sponge.server().commandManager().commandMapping(commandName);

        if (commandMapping.isEmpty()) return null;

        CommandMapping cmd = commandMapping.get();

        return new CommonExternalCommand(null, cmd.primaryAlias(), new ArrayList<>(cmd.allAliases()));
    }
}
