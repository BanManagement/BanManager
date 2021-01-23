package me.confuser.banmanager.common.storage.conversion;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.configs.LocalDatabaseConfig;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.storage.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class H2 implements IConverter {
  private BanManagerPlugin plugin;
  private PlayerBanStorage playerBanStorage;
  private PlayerBanRecordStorage playerBanRecordStorage;
  private PlayerKickStorage playerKickStorage;
  private PlayerMuteStorage playerMuteStorage;
  private PlayerMuteRecordStorage playerMuteRecordStorage;
  private PlayerStorage playerStorage;
  private PlayerWarnStorage playerWarnStorage;
  private PlayerNoteStorage playerNoteStorage;
  private PlayerHistoryStorage playerHistoryStorage;
  private PlayerReportStorage playerReportStorage;
  private PlayerReportLocationStorage playerReportLocationStorage;
  private ReportStateStorage reportStateStorage;
  private PlayerReportCommandStorage playerReportCommandStorage;
  private PlayerReportCommentStorage playerReportCommentStorage;

  private NameBanStorage nameBanStorage;
  private NameBanRecordStorage nameBanRecordStorage;

  private IpBanStorage ipBanStorage;
  private IpBanRecordStorage ipBanRecordStorage;
  private IpMuteStorage ipMuteStorage;
  private IpMuteRecordStorage ipMuteRecordStorage;
  private IpRangeBanStorage ipRangeBanStorage;
  private IpRangeBanRecordStorage ipRangeBanRecordStorage;

  public H2(BanManagerPlugin plugin, String fileName) {
    this.plugin = plugin;

    File file = new File(plugin.getDataFolder(), fileName + ".mv.db");

    if (!file.exists()) {
      plugin.getLogger().severe("Failed to find H2 database");
      return;
    }

    HashMap<String, DatabaseTableConfig<?>> tables = new HashMap<>();

    for (Map.Entry<String, Class> entry : LocalDatabaseConfig.types.entrySet()) {
      tables.put(entry.getKey(), new DatabaseTableConfig<>(entry.getValue(), plugin.getConfig().getLocalDb().getTable(entry.getKey()).getTableName(), null));
    }

    H2Config config = new H2Config("h2", "", 0, fileName, "", "", false, false, true, 5, 0, 1800000, 30000, tables, plugin.getDataFolder());

    ConnectionSource connection;
    try {
      connection = plugin.createConnection(config, "h2import");

      // Setup data storage
      playerStorage = new PlayerStorage(connection, config.getTable("players"));
      playerBanStorage = new PlayerBanStorage(connection, config.getTable("playerBans"));
      playerBanRecordStorage = new PlayerBanRecordStorage(connection, config.getTable("playerBanRecords"));
      playerMuteStorage = new PlayerMuteStorage(connection, config.getTable("playerMutes"));
      playerMuteRecordStorage = new PlayerMuteRecordStorage(connection, config.getTable("playerMuteRecords"));
      playerWarnStorage = new PlayerWarnStorage(connection, config.getTable("playerWarnings"));
      playerKickStorage = new PlayerKickStorage(connection, config.getTable("playerKicks"));
      playerNoteStorage = new PlayerNoteStorage(connection, config.getTable("playerNotes"));
      playerHistoryStorage = new PlayerHistoryStorage(connection, config.getTable("playerHistory"));
      reportStateStorage = new ReportStateStorage(connection, config.getTable("playerReportStates"));
      playerReportCommandStorage = new PlayerReportCommandStorage(connection, config.getTable("playerReportCommands"));
      playerReportCommentStorage = new PlayerReportCommentStorage(connection, config.getTable("playerReportComments"));
      playerReportStorage = new PlayerReportStorage(connection, config.getTable("playerReports"));
      playerReportLocationStorage = new PlayerReportLocationStorage(connection, config.getTable("playerReportLocations"));

      ipBanStorage = new IpBanStorage(connection, config.getTable("ipBans"));
      ipBanRecordStorage = new IpBanRecordStorage(connection, config.getTable("ipBanRecords"));
      ipMuteStorage = new IpMuteStorage(connection, config.getTable("ipMutes"));
      ipMuteRecordStorage = new IpMuteRecordStorage(connection, config.getTable("ipMuteRecords"));
      ipRangeBanStorage = new IpRangeBanStorage(connection, config.getTable("ipRangeBans"));
      ipRangeBanRecordStorage = new IpRangeBanRecordStorage(connection, config.getTable("ipRangeBanRecords"));

      nameBanStorage = new NameBanStorage(connection, config.getTable("nameBans"));
      nameBanRecordStorage = new NameBanRecordStorage(connection, config.getTable("nameBanRecords"));
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    importPlayers();
    importPlayerBans();
    importPlayerMutes();
    importPlayerWarnings();
    importPlayerKicks();
    importPlayerNotes();
    importPlayerHistory();
    importPlayerReports();
    importIpBans();
    importIpMutes();
    importIpRangeBans();
    importNameBans();

    connection.closeQuietly();
  }

  public void importPlayers() {
    plugin.getLogger().info("Importing players");

    try (CloseableIterator<PlayerData> itr = playerStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerData data = itr.next();

        try {
          plugin.getPlayerStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player " + data.getUUID());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing players");
  }

  @Override
  public void importPlayerBans() {
    plugin.getLogger().info("Importing player bans");

    try (CloseableIterator<PlayerBanData> itr = playerBanStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerBanData data = itr.next();

        try {
          plugin.getPlayerBanStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player ban " + data.getPlayer().getUUID());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player bans");

    plugin.getLogger().info("Importing player ban records");

    try (CloseableIterator<PlayerBanRecord> itr = playerBanRecordStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerBanRecord data = itr.next();

        try {
          plugin.getPlayerBanRecordStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player ban record " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player ban records");
  }

  @Override
  public void importPlayerMutes() {
    plugin.getLogger().info("Importing player mutes");

    try (CloseableIterator<PlayerMuteData> itr = playerMuteStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerMuteData data = itr.next();

        try {
          plugin.getPlayerMuteStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player mute " + data.getPlayer().getUUID());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player mutes");

    plugin.getLogger().info("Importing player mute records");

    try (CloseableIterator<PlayerMuteRecord> itr = playerMuteRecordStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerMuteRecord data = itr.next();

        try {
          plugin.getPlayerMuteRecordStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player mute record " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player mute records");
  }

  @Override
  public void importPlayerWarnings() {
    plugin.getLogger().info("Importing player warnings");

    try (CloseableIterator<PlayerWarnData> itr = playerWarnStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerWarnData data = itr.next();

        try {
          plugin.getPlayerWarnStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player warning " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player warnings");
  }

  public void importPlayerKicks() {
    plugin.getLogger().info("Importing player kicks");

    try (CloseableIterator<PlayerKickData> itr = playerKickStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerKickData data = itr.next();

        try {
          plugin.getPlayerKickStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player kick " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player kicks");
  }

  public void importPlayerNotes() {
    plugin.getLogger().info("Importing player notes");

    try (CloseableIterator<PlayerNoteData> itr = playerNoteStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerNoteData data = itr.next();

        try {
          plugin.getPlayerNoteStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player note " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player notes");
  }

  public void importPlayerHistory() {
    plugin.getLogger().info("Importing player history");

    try (CloseableIterator<PlayerHistoryData> itr = playerHistoryStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerHistoryData data = itr.next();

        try {
          plugin.getPlayerHistoryStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player note " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player history");
  }

  public void importPlayerReports() {
    plugin.getLogger().info("Importing player report states");

    try (CloseableIterator<ReportState> itr = reportStateStorage.closeableIterator()) {
      while (itr.hasNext()) {
        ReportState data = itr.next();

        try {
          plugin.getReportStateStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player report state " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player report states");

    plugin.getLogger().info("Importing player report commands");

    try (CloseableIterator<PlayerReportCommandData> itr = playerReportCommandStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerReportCommandData data = itr.next();

        try {
          plugin.getPlayerReportCommandStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player report state " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player report commands");

    plugin.getLogger().info("Importing player report comments");

    try (CloseableIterator<PlayerReportCommentData> itr = playerReportCommentStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerReportCommentData data = itr.next();

        try {
          plugin.getPlayerReportCommentStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player report comment " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player report comments");

    plugin.getLogger().info("Importing player reports");

    try (CloseableIterator<PlayerReportData> itr = playerReportStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerReportData data = itr.next();

        try {
          plugin.getPlayerReportStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player report " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player report");

    plugin.getLogger().info("Importing player report locations");

    try (CloseableIterator<PlayerReportLocationData> itr = playerReportLocationStorage.closeableIterator()) {
      while (itr.hasNext()) {
        PlayerReportLocationData data = itr.next();

        try {
          plugin.getPlayerReportLocationStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import player report location " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing player report locations");
  }

  @Override
  public void importIpBans() {
    plugin.getLogger().info("Importing ip bans");

    try (CloseableIterator<IpBanData> itr = ipBanStorage.closeableIterator()) {
      while (itr.hasNext()) {
        IpBanData data = itr.next();

        try {
          plugin.getIpBanStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import ip ban " + data.getIp());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing ip bans");

    plugin.getLogger().info("Importing ip ban records");

    try (CloseableIterator<IpBanRecord> itr = ipBanRecordStorage.closeableIterator()) {
      while (itr.hasNext()) {
        IpBanRecord data = itr.next();

        try {
          plugin.getIpBanRecordStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import ip ban record " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing ip ban records");
  }

  public void importIpMutes() {
    plugin.getLogger().info("Importing ip mutes");

    try (CloseableIterator<IpMuteData> itr = ipMuteStorage.closeableIterator()) {
      while (itr.hasNext()) {
        IpMuteData data = itr.next();

        try {
          plugin.getIpMuteStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import ip mute " + data.getIp());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing ip mutes");

    plugin.getLogger().info("Importing ip mute records");

    try (CloseableIterator<IpMuteRecord> itr = ipMuteRecordStorage.closeableIterator()) {
      while (itr.hasNext()) {
        IpMuteRecord data = itr.next();

        try {
          plugin.getIpMuteRecordStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import ip mute record " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing ip mute records");
  }

  @Override
  public void importIpRangeBans() {
    plugin.getLogger().info("Importing ip range bans");

    try (CloseableIterator<IpRangeBanData> itr = ipRangeBanStorage.closeableIterator()) {
      while (itr.hasNext()) {
        IpRangeBanData data = itr.next();

        try {
          plugin.getIpRangeBanStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import ip range ban " + data.getFromIp() + " - " + data.getToIp());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing ip range bans");

    plugin.getLogger().info("Importing ip range ban records");

    try (CloseableIterator<IpRangeBanRecord> itr = ipRangeBanRecordStorage.closeableIterator()) {
      while (itr.hasNext()) {
        IpRangeBanRecord data = itr.next();

        try {
          plugin.getIpRangeBanRecordStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import ip range ban record " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing ip range ban records");
  }

  public void importNameBans() {
    plugin.getLogger().info("Importing name bans");

    try (CloseableIterator<NameBanData> itr = nameBanStorage.closeableIterator()) {
      while (itr.hasNext()) {
        NameBanData data = itr.next();

        try {
          plugin.getNameBanStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import name ban " + data.getName());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing name bans");

    plugin.getLogger().info("Importing name ban records");

    try (CloseableIterator<NameBanRecord> itr = nameBanRecordStorage.closeableIterator()) {
      while (itr.hasNext()) {
        NameBanRecord data = itr.next();

        try {
          plugin.getNameBanRecordStorage().createIfNotExists(data);
        } catch (SQLException e) {
          e.printStackTrace();
          plugin.getLogger().severe("Failed to import name ban record " + data.getId());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    plugin.getLogger().info("Finished importing name ban records");
  }

  class H2Config extends DatabaseConfig {
    public H2Config(String storageType, String host, int port, String name, String user, String password, boolean useSSL, boolean verifyServerCertificate, boolean isEnabled, int maxConnections, int leakDetection, int maxLifetime, int connectionTimeout, HashMap<String, DatabaseTableConfig<?>> tables, File dataFolder) {
      super(storageType, host, port, name, user, password, useSSL, verifyServerCertificate, isEnabled, maxConnections, leakDetection, maxLifetime, connectionTimeout, tables, dataFolder);
    }
  }
}
