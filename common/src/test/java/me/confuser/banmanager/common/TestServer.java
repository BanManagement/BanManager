package me.confuser.banmanager.common;

import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.kyori.text.TextComponent;

import java.sql.SQLException;
import java.util.UUID;

public class TestServer implements CommonServer {
  private BanManagerPlugin plugin;

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    try {
      PlayerData player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uniqueId));

      if (player == null) return null;

      return new TestPlayer(uniqueId, player.getName(), true);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    PlayerData player = plugin.getPlayerStorage().retrieve(name, false);

    if (player == null) return null;

    return new TestPlayer(player.getUUID(), player.getName(), true);
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return new CommonPlayer[0];
  }

  @Override
  public void broadcast(String message, String permission) {
  }

  @Override
  public void broadcastJSON(TextComponent message, String permission) {
  }

  @Override
  public void broadcast(String message, String permission, CommonSender sender) {
  }

  @Override
  public CommonSender getConsoleSender() {
    PlayerData console = plugin.getPlayerStorage().getConsole();

    return new TestSender(console.getUUID(), console.getName(), true);
  }

  @Override
  public boolean dispatchCommand(CommonSender consoleSender, String command) {
    return true;
  }

  @Override
  public CommonWorld getWorld(String name) {
    return new CommonWorld(name);
  }

  @Override
  public CommonEvent callEvent(String name, Object... args) {
    return new CommonEvent(false, false);
  }

  public void enable(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public CommonExternalCommand getPluginCommand(String commandName) {
    return null;
  }
}
