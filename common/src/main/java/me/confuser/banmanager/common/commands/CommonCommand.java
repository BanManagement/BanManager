package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;

public abstract class CommonCommand {

  @Getter
  private static BanManagerPlugin plugin;
  @Getter
  private final String permission;
  @Getter
  private final String commandName;

  public CommonCommand(BanManagerPlugin plugin, String commandName) {
    this.plugin = plugin;
    this.commandName = commandName;
    this.permission = "bm.command." + commandName;
  }

  public static boolean isUUID(String player) {
    return player.length() > 16;
  }

  public static PlayerData getPlayer(CommonSender sender, String playerName, boolean mojangLookup) {
    boolean isUUID = isUUID(playerName);
    PlayerData player = null;

    if (isUUID) {
      try {
        player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
      }
    } else {
      player = plugin.getPlayerStorage().retrieve(playerName, mojangLookup);
    }

    return player;
  }

  public static void handlePunishmentCreateException(SQLException e, CommonSender sender, Message duplicateMessage) {
    // For some reason ORMLite hides the error code (returns 0 instead of 1062)
    if (e.getCause().getMessage().startsWith("Duplicate entry")) {
      duplicateMessage.sendTo(sender);
      return;
    }

    Message.get("sender.error.exception").sendTo(sender);
    e.printStackTrace();
  }

  public static void handlePrivateNotes(PlayerData player, PlayerData actor, Reason reason) {
    if (plugin.getConfig().isCreateNoteReasons())
      if (reason.getNotes().size() == 0) return;

    for (String note : reason.getNotes()) {
      try {
        plugin.getPlayerNoteStorage().create(new PlayerNoteData(player, actor, note));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

  }

  public static Long getIp(String ipStr) {
    final boolean isName = !InetAddresses.isInetAddress(ipStr);
    Long ip = null;

    if (isName) {
      PlayerData player = plugin.getPlayerStorage().retrieve(ipStr, false);
      if (player == null) return ip;

      ip = player.getIp();
    } else {
      ip = IPUtils.toLong(ipStr);
    }

    return ip;
  }

  public abstract boolean onCommand(final CommonSender sender, CommandParser args);
}
