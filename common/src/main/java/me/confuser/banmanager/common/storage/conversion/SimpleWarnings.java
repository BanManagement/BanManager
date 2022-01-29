package me.confuser.banmanager.common.storage.conversion;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.ormlite.jdbc.JdbcPooledConnectionSource;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SimpleWarnings implements IConverter {

  private BanManagerPlugin plugin;
  private JdbcPooledConnectionSource connection;

  public SimpleWarnings(BanManagerPlugin plugin) {
    this.plugin = plugin;

    try {
      connection = new JdbcPooledConnectionSource("jdbc:sqlite:" + new File(plugin.getDataFolder().getParent(),
              "SimpleWarnings/Warnings.db").getAbsolutePath());
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to connect to SimpleWarnings database");
      return;
    }

    connection.setMaxConnectionsFree(1);
    try {
      connection.initialize();
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to connect to SimpleWarnings database");
      return;
    }

    importPlayerWarnings();

    connection.closeQuietly();
  }


  @Override
  public void importPlayerWarnings() {
    DatabaseConnection read;
    int count = 0;

    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to connect to SimpleWarnings database");
      return;
    }

    DatabaseResults results;

    try {
      results = read
              .compileStatement("SELECT `name`, `warning`, `placedby`, `date` FROM SimpleWarnings", StatementBuilder
                      .StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false)
              .runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    try {
      while (results.next()) {
        String name = results.getString(0);
        String reason = results.getString(1);
        String actorName = results.getString(2);
        Timestamp created = results.getTimestamp(3);

        PlayerData playerData = plugin.getPlayerStorage().retrieve(name, true);
        PlayerData actor = plugin.getPlayerStorage().retrieve(actorName, false);

        if (playerData == null) {
          plugin.getLogger().severe(name + " warning creation failed, unable to lookup UUID");
          continue;
        }

        if (actor == null) {
          actor = plugin.getPlayerStorage().getConsole();
        }

        PlayerWarnData data = new PlayerWarnData(playerData, actor, reason, true, 0, created.getTime() / 1000L);

        // Disallow duplicates
        if (plugin.getPlayerWarnStorage().queryForMatchingArgs(data).size() != 0) continue;

        plugin.getPlayerWarnStorage().addWarning(data, true);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
    }

    read.closeQuietly();
    plugin.getLogger().info("Imported " + count + " rows from SimpleWarnings");
  }

  @Override
  public void importPlayerMutes() {
  }

  @Override
  public void importPlayerBans() {
  }

  @Override
  public void importIpBans() {
  }

  @Override
  public void importIpRangeBans() {
  }
}
