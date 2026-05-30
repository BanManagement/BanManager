package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.event.player.PlayerReportedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Stores the Sponge-specific player and actor location at the time a report
 * is filed. Notification dispatch and reference cleanup live in the
 * cross-platform {@link me.confuser.banmanager.common.listeners.CommonReportListener}.
 */
public class ReportListener {
    private final BanManagerPlugin plugin;

    public ReportListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
        plugin.getEventBus().subscribe(PlayerReportedEvent.class, this::storeLocation);
    }

    private void storeLocation(PlayerReportedEvent event) {
        PlayerReport report = event.report();
        PlayerReportData entity;
        try {
            entity = plugin.getPlayerReportStorage().queryForId(report.id());
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load report entity for location storage", e);
            return;
        }
        if (entity == null) return;

        Optional<ServerPlayer> player = Sponge.server().player(report.player().uuid());
        Optional<ServerPlayer> actor = Sponge.server().player(report.actor().uuid());

        try {
            if (player.isPresent()) createLocation(entity, player.get());
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to store report location for reported player", e);
        }

        try {
            if (actor.isPresent()) createLocation(entity, actor.get());
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to store report location for actor", e);
        }
    }

    private void createLocation(PlayerReportData report, ServerPlayer player) throws SQLException {
        if (player == null) return;

        PlayerData playerData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player.uniqueId()));
        ServerLocation loc = player.serverLocation();

        plugin.getPlayerReportLocationStorage()
            .create(new PlayerReportLocationData(
                report,
                playerData,
                loc.world().key().asString(),
                loc.x(),
                loc.y(),
                loc.z(),
                0,
                0
            ));
    }
}
