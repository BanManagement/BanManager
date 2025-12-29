package me.confuser.banmanager.common.storage.conversion;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.BanIpCommand;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;

public class AdvancedBan implements IConverter {
  private ConnectionSource connection;
  private BanManagerPlugin plugin;

  public AdvancedBan(BanManagerPlugin plugin, String[] args) {
    this.plugin = plugin;

    String host = args[1];
    int port = Integer.parseInt(args[2]);
    String database = args[3];
    String username = args[4];
    String password = args.length == 6 ? args[5] : "";

    AdvancedBanConfig config = new AdvancedBanConfig(
        "mysql", host, port, database, username, password,
        false, false, true, true, 2, 0, 1800000, 30000,
        new HashMap<>(), plugin.getDataFolder()
    );

    try {
      connection = plugin.createConnection(config, "advancedban-import");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to connect to AdvancedBan database");
      return;
    }

    importPunishments();

    connection.closeQuietly();
  }

  public void importPunishments() {
    DatabaseConnection read;
    int count = 0;

    try {
      read = connection.getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to connect to AdvancedBan database");
      return;
    }

    DatabaseResults results;

    try {
      results = read
          .compileStatement("SELECT `name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end` FROM `Punishments`", StatementBuilder
              .StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS, false)
          .runQuery(null);
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    try {
      while (results.next()) {
        String name = results.getString(0);
        String uuid = results.getString(1);
        String reason = results.getString(2);
        String actorName = results.getString(3);
        String type = results.getString(4);
        long created = Long.parseLong(results.getString(5)) / 1000L;
        String end = results.getString(6);

        PlayerData actor = plugin.getPlayerStorage().retrieve(actorName, false);

        if (actor == null) {
          actor = plugin.getPlayerStorage().getConsole();
        }

        long expires = 0;

        if (!end.equals("-1")) expires = Long.parseLong(end) / 1000L;

        if (type.equalsIgnoreCase("BAN") || type.equalsIgnoreCase("TEMP_BAN")) {
          PlayerData playerData = plugin.getPlayerStorage().retrieve(name, true);

          if (playerData == null) {
            plugin.getLogger().severe(name + " ban creation failed, unable to lookup UUID");
            continue;
          }

          PlayerBanData data = new PlayerBanData(playerData, actor, reason, true, expires, created);

          if (!plugin.getPlayerBanStorage().isBanned(playerData.getUUID())) {
            plugin.getPlayerBanStorage().ban(data);
          }
        } else if (type.equalsIgnoreCase("MUTE") || type.equalsIgnoreCase("TEMP_MUTE")) {
          PlayerData playerData = plugin.getPlayerStorage().retrieve(name, true);

          if (playerData == null) {
            plugin.getLogger().severe(name + " mute creation failed, unable to lookup UUID");
            continue;
          }

          PlayerMuteData data = new PlayerMuteData(playerData, actor, reason, true, false, expires, created);

          if (!plugin.getPlayerMuteStorage().isMuted(playerData.getUUID())) {
            plugin.getPlayerMuteStorage().mute(data);
          }
        } else if (type.equalsIgnoreCase("IP_BAN") || type.equalsIgnoreCase("TEMP_IP_BAN")) {
          IPAddress ip = BanIpCommand.getIp(uuid);

          if (ip == null) {
            plugin.getLogger().severe(name + " ip ban creation failed, invalid ip");
            continue;
          }

          IpBanData data = new IpBanData(ip, actor, reason, true, expires, created);

          if (!plugin.getIpBanStorage().isBanned(ip)) {
            plugin.getIpBanStorage().ban(data);
          }
        } else if (type.equalsIgnoreCase("WARNING") || type.equalsIgnoreCase("TEMP_WARNING")) {
          PlayerData playerData = plugin.getPlayerStorage().retrieve(name, true);

          if (playerData == null) {
            plugin.getLogger().severe(name + " warning creation failed, unable to lookup UUID");
            continue;
          }

          PlayerWarnData data = new PlayerWarnData(playerData, actor, reason, true, expires, created);

          CloseableIterator<PlayerWarnData> warnings = plugin.getPlayerWarnStorage().getWarnings(playerData);

          while (warnings.hasNext()) {
            PlayerWarnData warn = warnings.next();

            if (warn.getReason().equalsIgnoreCase(reason) && warn.getActor().getUUID().equals(actor.getUUID())) {
              continue;
            }

            plugin.getPlayerWarnStorage().addWarning(data, true);
          }
        }

        count++;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      results.closeQuietly();
    }

    read.closeQuietly();
    plugin.getLogger().info("Imported " + count + " punishments from AdvancedBan");
  }

  @Override
  public void importPlayerBans() {
  }

  @Override
  public void importPlayerMutes() {
  }

  @Override
  public void importPlayerWarnings() {
  }

  @Override
  public void importIpBans() {
  }

  @Override
  public void importIpRangeBans() {
  }

  /**
   * Config class for AdvancedBan database connection.
   */
  static class AdvancedBanConfig extends DatabaseConfig {
    public AdvancedBanConfig(String storageType, String host, int port, String name, String user, String password,
                              boolean useSSL, boolean verifyServerCertificate, boolean allowPublicKeyRetrieval,
                              boolean isEnabled, int maxConnections, int leakDetection, int maxLifetime,
                              int connectionTimeout, HashMap<String, DatabaseTableConfig<?>> tables, File dataFolder) {
      super(storageType, host, port, name, user, password, useSSL, verifyServerCertificate, allowPublicKeyRetrieval,
          isEnabled, maxConnections, leakDetection, maxLifetime, connectionTimeout, tables, dataFolder);
    }
  }
}
