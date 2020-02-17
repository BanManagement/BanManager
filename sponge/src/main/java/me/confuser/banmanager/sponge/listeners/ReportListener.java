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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.SQLException;
import java.util.Optional;

public class ReportListener {
  private final CommonReportListener listener;
  private BanManagerPlugin plugin;

  public ReportListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonReportListener(plugin);
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnReport(PlayerReportedEvent event) {
    listener.notifyOnReport(event.getReport());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void storeLocation(PlayerReportedEvent event) {
    PlayerReportData report = event.getReport();

    Optional<Player> player = Sponge.getServer().getPlayer(report.getPlayer().getUUID());
    Optional<Player> actor = Sponge.getServer().getPlayer(report.getActor().getUUID());

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

  private void createLocation(PlayerReportData report, Player player) throws SQLException {
    if (player == null) return;

    PlayerData playerData = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player.getUniqueId()));
    Location<World> loc = player.getLocation();

    plugin.getPlayerReportLocationStorage()
        .create(new PlayerReportLocationData(report, playerData, player.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), 0, 0));
  }

  @Listener
  public void deleteReferences(PlayerReportDeletedEvent event) {
    listener.deleteReferences(event.getReport());
  }
}
