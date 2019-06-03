package me.confuser.banmanager.common.commands.utils;

import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.CommandException;
import me.confuser.banmanager.common.command.abstraction.SubCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MissingPlayersSubCommand extends SubCommand<String> {

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

  public MissingPlayersSubCommand(LocaleManager locale) {
    super(CommandSpec.BMUTILS_MISSINGPLAYERS.localize(locale), "missingplayers", CommandPermission.BMUTILS_MISSINGPLAYERS, Predicates.alwaysFalse());
  }

  //command.bmutils.missingplayers

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, String s, List<String> args, String label) throws CommandException {
    sender.sendMessage("Scanning database");

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      DatabaseConnection connection;
      ArrayList<UUID> players = new ArrayList<>();

      try {
        connection = plugin.getLocalConn().getReadOnlyConnection("");
      } catch (SQLException e) {
        e.printStackTrace();

        Message.SENDER_ERROR_EXCEPTION.send(sender);

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
                          .DEFAULT_RESULT_FLAGS, false);

          result = statement.runQuery(null);

          while (result.next()) {
            players.add(UUIDUtils.fromBytes(result.getBytes(0)));
          }
        } catch (SQLException e) {
          e.printStackTrace();

          Message.SENDER_ERROR_EXCEPTION.send(sender);
        } finally {
          if (result != null) result.closeQuietly();
        }
      }

      try {
        plugin.getLocalConn().releaseConnection(connection);
      } catch (SQLException e) {
        e.printStackTrace();
      }

      if (players.size() == 0) {
        Message.BMUTILS_MISSINGPLAYERS_NONEFOUND.send(sender);
        return;
      }

      Message.BMUTILS_MISSINGPLAYERS_FOUND.send(sender, "amount", players.size());

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

          Message.BMUTILS_MISSINGPLAYERS_ERROR_FAILEDLOOKUP.send(sender,"uuid", uuid.toString());
        }
      }

      Message.BMUTILS_MISSINGPLAYERS_COMPLETE.send(sender, "amount", count);

    });

    return CommandResult.SUCCESS;
  }

}
