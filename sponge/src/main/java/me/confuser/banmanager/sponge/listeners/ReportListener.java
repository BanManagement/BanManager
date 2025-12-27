package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.listeners.CommonReportListener;
import me.confuser.banmanager.common.util.UUIDUtils;
import me.confuser.banmanager.sponge.api.events.PlayerReportDeletedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerReportedEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.Optional;

public class ReportListener {
    private final CommonReportListener listener;
    private final BanManagerPlugin plugin;

    public ReportListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.listener = new CommonReportListener(plugin);
    }

    @Listener(order = Order.POST)
    public void notifyOnReport(PlayerReportedEvent event) {
        listener.notifyOnReport(event.getReport());
    }

    @Listener(order = Order.POST)
    public void storeLocation(PlayerReportedEvent event) {
        PlayerReportData report = event.getReport();

        Optional<ServerPlayer> player = Sponge.server().player(report.getPlayer().getUUID());
        Optional<ServerPlayer> actor = Sponge.server().player(report.getActor().getUUID());

        try {
            if (player.isPresent()) createLocation(report, player.get());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (actor.isPresent()) createLocation(report, actor.get());
        } catch (SQLException e) {
            e.printStackTrace();
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

    @Listener
    public void deleteReferences(PlayerReportDeletedEvent event) {
        listener.deleteReferences(event.getReport());
    }
}
