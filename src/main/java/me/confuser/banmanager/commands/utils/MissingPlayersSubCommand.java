package me.confuser.banmanager.commands.utils;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MissingPlayersSubCommand extends SubCommand<BanManager> {

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

  public MissingPlayersSubCommand() {
    super("missingplayers");
  }

  @Override
  public boolean onCommand(final CommandSender sender, final String[] args) {
    sender.sendMessage("Scanning database");

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        DatabaseConnection connection;
        ArrayList<UUID> players = new ArrayList<UUID>();

        try {
          connection = plugin.getLocalConn().getReadOnlyConnection();
        } catch (SQLException e) {
          e.printStackTrace();

          Message.get("sender.error.exception").sendTo(sender);

          return;
        }

        String playerTableName = plugin.getConfiguration().getLocalDb().getTable("players").getTableName();

        for (String type : types) {
          String tableName = plugin.getConfiguration().getLocalDb().getTable(type).getTableName();
          String sql = "SELECT b.player_id FROM " + tableName + " b LEFT JOIN `" + playerTableName + "` p ON b.player_id = p.id WHERE p.id IS NULL";
          DatabaseResults result = null;

          try {
            CompiledStatement statement = connection
                    .compileStatement(sql, StatementBuilder.StatementType.SELECT, null, DatabaseConnection
                            .DEFAULT_RESULT_FLAGS);

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

        connection.closeQuietly();

        if (players.size() == 0) {
          Message.get("bmutils.missingplayers.noneFound").sendTo(sender);
          return;
        }

        Message.get("bmutils.missingplayers.found").set("amount", players.size()).sendTo(sender);

        int count = 0;

        for (UUID uuid : players) {
          try {
            String name = "Unknown";

            if (BanManager.getPlugin().getConfiguration().isOnlineMode()) {
              name = UUIDUtils.getCurrentName(uuid);
            }

            plugin.getPlayerStorage().createIfNotExists(uuid, name);

            count++;
          } catch (Exception e) {
            e.printStackTrace();

            Message.get("bmutils.missingplayers.failedLookup").set("uuid", uuid.toString()).sendTo(sender);
          }
        }

        Message.get("bmutils.missingplayers.complete").set("amount", count).sendTo(sender);
      }
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
