package me.confuser.banmanager.common.commands.utils;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.commands.CommonSubCommand;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MissingPlayersSubCommand extends CommonSubCommand {

  private static List<String> types = new ArrayList<String>() {

    {
      add("playerBans");
      add("playerKicks");
      add("playerMutes");
      add("playerNotes");
      add("playerReports");
      add("playerWarnings");
    }
  };

  public MissingPlayersSubCommand(BanManagerPlugin plugin) {
    super(plugin, "missingplayers");
  }

  @Override
  public boolean onCommand(final CommonSender sender, final CommandParser parser) {
    sender.sendMessage("Scanning database");

    getPlugin().getScheduler().runAsync(() -> {
      DatabaseConnection connection;
      ArrayList<UUID> players = new ArrayList<UUID>();

      try {
        connection = getPlugin().getLocalConn().getReadOnlyConnection("");
      } catch (SQLException e) {
        e.printStackTrace();

        Message.get("sender.error.exception").sendTo(sender);

        return;
      }

      String playerTableName = getPlugin().getConfig().getLocalDb().getTable("players").getTableName();

      for (String type : types) {
        String tableName = getPlugin().getConfig().getLocalDb().getTable(type).getTableName();
        String sql = "SELECT b.player_id FROM " + tableName + " b LEFT JOIN `" + playerTableName + "` p ON b.player_id = p.id WHERE p.id IS NULL";
        DatabaseResults result = null;

        try {
          CompiledStatement statement = connection
                  .compileStatement(sql, StatementBuilder.StatementType.SELECT, null, DatabaseConnection
                          .DEFAULT_RESULT_FLAGS, false);

          result = statement.runQuery(null);

          while (result.next()) {
            players.add(UUIDUtils.fromBytes(result.getBytes(0)));
          }
        } catch (SQLException e) {
          e.printStackTrace();

          Message.get("sender.error.exception").sendTo(sender);
        } finally {
          if (result != null) result.closeQuietly();
        }
      }

      try {
        getPlugin().getLocalConn().releaseConnection(connection);
      } catch (SQLException e) {
        e.printStackTrace();
      }

      if (players.size() == 0) {
        Message.get("bmutils.missingplayers.noneFound").sendTo(sender);
        return;
      }

      Message.get("bmutils.missingplayers.found").set("amount", players.size()).sendTo(sender);

      int count = 0;

      for (UUID uuid : players) {
        try {
          String name = "Unknown";

          if (getPlugin().getConfig().isOnlineMode()) {
            name = UUIDUtils.getCurrentName(getPlugin(), uuid);
          }

          getPlugin().getPlayerStorage().createIfNotExists(uuid, name);

          count++;
        } catch (Exception e) {
          e.printStackTrace();

          Message.get("bmutils.missingplayers.failedLookup").set("uuid", uuid.toString()).sendTo(sender);
        }
      }

      Message.get("bmutils.missingplayers.complete").set("amount", count).sendTo(sender);
    });

    return true;
  }

  @Override
  public String getHelp() {
    return null;
  }

  @Override
  public String getPermission() {
    return "command.bmutils.missingplayers";
  }
}
