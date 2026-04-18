package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;
import me.confuser.banmanager.common.util.MessageRegistry;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SpongePlayer implements CommonPlayer {
    private ServerPlayer player;
    private final UUID uuid;
    private final boolean onlineMode;
    private InetAddress address;

    public SpongePlayer(UUID uuid, String name, boolean onlineMode) {
        this.uuid = uuid;
        this.onlineMode = onlineMode;
    }

    public SpongePlayer(ServerPlayer player, boolean onlineMode) {
        this(player.uniqueId(), player.name(), onlineMode);
        this.player = player;
    }

    public SpongePlayer(ServerPlayer player, boolean onlineMode, InetAddress address) {
        this(player, onlineMode);
        this.address = address;
    }

    @Override
    public void kick(String message) {
        getPlayer().kick(SpongeServer.formatMessage(message));
    }

    @Override
    public void kick(me.confuser.banmanager.common.kyori.text.Component component) {
        getPlayer().kick(convertToNative(component));
    }

    @Override
    public void sendMessage(String message) {
        if (message.isEmpty()) return;

        if (Message.isJSONMessage(message)) {
            sendJSONMessage(message);
        } else {
            getPlayer().sendMessage(SpongeServer.formatMessage(message));
        }
    }

    @Override
    public void sendMessage(me.confuser.banmanager.common.kyori.text.Component component) {
        getPlayer().sendMessage(convertToNative(component));
    }

    @Override
    public void sendActionBar(me.confuser.banmanager.common.kyori.text.Component component) {
        getPlayer().sendActionBar(convertToNative(component));
    }

    @Override
    public void showTitle(me.confuser.banmanager.common.kyori.text.Component title,
                          me.confuser.banmanager.common.kyori.text.Component subtitle,
                          int fadeIn, int stay, int fadeOut) {
        Component nativeTitle = title != null ? convertToNative(title) : Component.empty();
        Component nativeSubtitle = subtitle != null ? convertToNative(subtitle) : Component.empty();
        Title.Times times = Title.Times.times(
            java.time.Duration.ofMillis(fadeIn * 50L),
            java.time.Duration.ofMillis(stay * 50L),
            java.time.Duration.ofMillis(fadeOut * 50L)
        );
        getPlayer().showTitle(Title.title(nativeTitle, nativeSubtitle, times));
    }

    @Override
    public void playSound(String sound, float volume, float pitch) {
        ServerPlayer p = getPlayer();
        if (p == null) return;
        try {
            p.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER, volume, pitch));
        } catch (IllegalArgumentException ignored) {
            // Invalid sound key -- silently ignore
        }
    }

    @Override
    public void sendJSONMessage(TextComponent jsonString) {
        getPlayer().sendMessage(SpongeServer.formatMessage(jsonString));
    }

    @Override
    public void sendJSONMessage(String jsonString) {
        getPlayer().sendMessage(SpongeServer.formatJsonMessage(jsonString));
    }

    private Component convertToNative(me.confuser.banmanager.common.kyori.text.Component component) {
        String json = MessageRenderer.getInstance().toJson(component);
        return GsonComponentSerializer.gson().deserialize(json);
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public PlayerData getData() {
        try {
            return BanManagerPlugin.getInstance().getPlayerStorage().queryForId(UUIDUtils.toBytes(getUniqueId()));
        } catch (SQLException e) {
            BanManagerPlugin.getInstance().getLogger().warning("Failed to load player data", e);
            sendMessage(Message.get("sender.error.exception").toString());
            return null;
        }
    }

    @Override
    public boolean isOnlineMode() {
        return onlineMode;
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public boolean hasPermission(String permission) {
        ServerPlayer p = getPlayer();
        if (p == null) return false;

        org.spongepowered.api.util.Tristate tristate = p.permissionValue(permission);
        if (tristate != org.spongepowered.api.util.Tristate.UNDEFINED) {
            return tristate.asBoolean();
        }

        if (permission.startsWith("bm.")) {
            return isOperator(p);
        }

        return false;
    }

    private boolean isOperator(ServerPlayer player) {
        return player.hasPermission("minecraft.command.op") ||
               player.hasPermission("minecraft.command.ban") ||
               player.hasPermission("minecraft.command.kick");
    }

    @Override
    public String getDisplayName() {
        ServerPlayer p = getPlayer();
        if (p == null) return "";
        Component displayName = p.displayName().get();
        return LegacyComponentSerializer.legacyAmpersand().serialize(displayName);
    }

    @Override
    public String getName() {
        ServerPlayer p = getPlayer();
        return p != null ? p.name() : "";
    }

    @Override
    public InetAddress getAddress() {
        if (address != null) return address;
        ServerPlayer p = getPlayer();
        return p != null ? p.connection().address().getAddress() : null;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
        ServerPlayer p = getPlayer();
        if (p == null) return false;

        Optional<ServerWorld> worldOpt = Sponge.server().worldManager().world(
            org.spongepowered.api.ResourceKey.resolve(world.getName())
        );

        if (worldOpt.isPresent()) {
            ServerLocation location = ServerLocation.of(worldOpt.get(), x, y, z);
            return p.setLocation(location);
        }

        return false;
    }

    @Override
    public boolean canSee(CommonPlayer player) {
        ServerPlayer p = getPlayer();
        if (p == null) return false;

        Optional<ServerPlayer> target = Sponge.server().player(player.getUniqueId());
        return target.map(serverPlayer -> p.canSee(serverPlayer)).orElse(false);
    }

    @Override
    public String getLocale() {
        ServerPlayer p = getPlayer();
        if (p == null) return "en";
        return MessageRegistry.normaliseLocale(p.locale().toString());
    }

    private ServerPlayer getPlayer() {
        if (player != null && !player.isRemoved()) {
            return player;
        }

        if (isOnlineMode()) {
            return Sponge.server().player(uuid).orElse(null);
        }

        for (ServerPlayer onlinePlayer : Sponge.server().onlinePlayers()) {
            if (UUIDUtils.createOfflineUUID(onlinePlayer.name()).equals(uuid)) {
                return onlinePlayer;
            }
        }

        return null;
    }
}
