package me.confuser.banmanager.common.storage.conversion;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.BanIpCommand;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.jdbc.JdbcPooledConnectionSource;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;

import java.sql.SQLException;

public class AdvancedBan implements IConverter {
  private JdbcPooledConnectionSource connection;
  private BanManagerPlugin plugin;
  private String host;
  private String port;
  private String database;
  private String username;
  private String password;

  public AdvancedBan(BanManagerPlugin plugin, String[] args) {
    this.plugin = plugin;
    this.host = args[1];
    this.port = args[2];
    this.database = args[3];
    this.username = args[4];

    if (args.length == 6) this.password = args[5];

    try {
      connection = new JdbcPooledConnectionSource("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Failed to connect to AdvancedBan database");
      return;
    }

    connection.setMaxConnectionsFree(1);
    try {
      connection.initialize();
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
}
