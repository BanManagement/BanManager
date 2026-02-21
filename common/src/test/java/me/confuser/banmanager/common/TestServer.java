package me.confuser.banmanager.common;

import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TestServer implements CommonServer {
  private BanManagerPlugin plugin;
  private boolean useStorageForOnlineLookups = true;
  private final Map<UUID, CommonPlayer> exactUuidMatches = new HashMap<>();
  private final Map<String, CommonPlayer> exactNameMatches = new HashMap<>();
  private final Map<String, CommonPlayer> partialMatches = new HashMap<>();

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    CommonPlayer exact = exactUuidMatches.get(uniqueId);
    if (exact != null) {
      return exact;
    }

    if (!useStorageForOnlineLookups) {
      return null;
    }

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
    CommonPlayer exact = getPlayerExact(name);
    if (exact != null) {
      return exact;
    }

    CommonPlayer partial = partialMatches.get(name.toLowerCase(Locale.ROOT));
    if (partial != null) {
      return partial;
    }

    if (!useStorageForOnlineLookups) {
      return null;
    }

    PlayerData player = plugin.getPlayerStorage().retrieve(name, false);
    if (player == null) return null;

    return new TestPlayer(player.getUUID(), player.getName(), true);
  }

  @Override
  public CommonPlayer getPlayerExact(String name) {
    return exactNameMatches.get(name.toLowerCase(Locale.ROOT));
  }

  public void setPartialMatch(String input, CommonPlayer player) {
    partialMatches.put(input.toLowerCase(Locale.ROOT), player);
  }

  public void setExactMatch(String input, CommonPlayer player) {
    exactNameMatches.put(input.toLowerCase(Locale.ROOT), player);
  }

  public void setExactMatch(UUID uuid, CommonPlayer player) {
    exactUuidMatches.put(uuid, player);
  }

  public void setUseStorageForOnlineLookups(boolean useStorageForOnlineLookups) {
    this.useStorageForOnlineLookups = useStorageForOnlineLookups;
  }

  public void clearPartialMatches() {
    partialMatches.clear();
  }

  public void clearExactMatches() {
    exactNameMatches.clear();
    exactUuidMatches.clear();
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
