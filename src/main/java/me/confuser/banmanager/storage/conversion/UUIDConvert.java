package me.confuser.banmanager.storage.conversion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.ConvertDatabaseConfig;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpBanRecord;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerKickData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.PlayerMuteRecord;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.storage.PlayerStorage;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;

// This class is horrifically big, lots of repetition, but speed for release over readability :(
// This class will be removed in upcoming releases
public class UUIDConvert {

      private BanManager plugin = BanManager.getPlugin();
      private PlayerStorage playerStorage = plugin.getPlayerStorage();
      private ConvertDatabaseConfig conversionDb = plugin.getConfiguration().getConversionDb();

      public UUIDConvert(JdbcPooledConnectionSource conversionConn) {
            // Convert player ips table first
            DatabaseConnection connection;
            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            convertPlayerIpTable(connection);
            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            plugin.getLogger().info(ChatColor.GREEN + "Player ips table converted!");
            plugin.getLogger().info("Starting bans table conversion");

            // Convert bans!
            convertBansTable(connection);

            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            plugin.getLogger().info(ChatColor.GREEN + "bans table converted!");
            plugin.getLogger().info("Starting ban records table conversion");

            // Convert ban records
            convertBanRecordsTable(connection);

            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            plugin.getLogger().info(ChatColor.GREEN + "Player ban records table converted!");
            plugin.getLogger().info("Starting mutes table conversion");

            // Convert mutes!
            convertMutesTable(connection);

            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            plugin.getLogger().info(ChatColor.GREEN + "mutes table converted!");
            plugin.getLogger().info("Starting mute records table conversion");

            // Convert ban records
            convertMuteRecordsTable(connection);

            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            plugin.getLogger().info(ChatColor.GREEN + "Mute records table converted!");
            plugin.getLogger().info("Starting warnings table conversion");

            convertWarningsTable(connection);

            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            plugin.getLogger().info(ChatColor.GREEN + "Player warning records table converted!");
            plugin.getLogger().info("Starting kicks table conversion");

            convertKicksTable(connection);

            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            plugin.getLogger().info(ChatColor.GREEN + "Player kicks table converted!");
            plugin.getLogger().info("Starting ip bans table conversion");

            // Convert mutes!
            convertIpBansTable(connection);

            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            try {
                  connection = conversionConn.getReadOnlyConnection();
            } catch (SQLException e) {
                  e.printStackTrace();
                  plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
                  return;
            }

            plugin.getLogger().info(ChatColor.GREEN + "ip bans table converted!");
            plugin.getLogger().info("Starting ip bans records table conversion");

            // Convert ban records
            convertIpBanRecordsTable(connection);

            try {
                  conversionConn.releaseConnection(connection);
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            plugin.getLogger().info(ChatColor.GREEN + "Ip ban records table converted!");
            plugin.getLogger().info(ChatColor.GREEN + "Conversion complete! Please check logs for errors. Restart the server for new data to take affect!");
            conversionConn.closeQuietly();

            plugin.getConfiguration().conf.set("databases.convert.enabled", false);
            plugin.getConfiguration().save();
      }

      private void convertIpBanRecordsTable(DatabaseConnection connection) {
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time FROM " + conversionDb.getTableName("ipBansRecordTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }

            try {
                  while (result.next()) {
                        String ip = result.getString(0);
                        String actorName = result.getString(1);
                        String reason = result.getString(2);
                        long pastCreated = result.getLong(3);
                        long expires = result.getLong(4);
                        String unbannedActorName = result.getString(5);
                        long created = result.getLong(6);

                        PlayerData banActor = playerStorage.retrieve(actorName, false);
                        PlayerData actor = playerStorage.retrieve(unbannedActorName, false);

                        if (actor == null) {
                              actor = playerStorage.getConsole();
                        }

                        if (banActor == null) {
                              banActor = playerStorage.getConsole();
                        }

                        IpBanData ban = new IpBanData(IPUtils.toLong(ip), banActor, reason, expires, pastCreated);
                        IpBanRecord record = new IpBanRecord(ban, actor, created);

                        plugin.getIpBanRecordStorage().create(record);
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }
      }

      private void convertIpBansTable(DatabaseConnection connection) {
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT banned, banned_by, ban_reason, ban_time, ban_expires_on FROM " + conversionDb.getTableName("ipBansTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }

            try {
                  while (result.next()) {
                        String ip = result.getString(0);
                        String actorName = result.getString(1);
                        String reason = result.getString(2);
                        long created = result.getLong(3);
                        long expires = result.getLong(4);

                        PlayerData actor = playerStorage.retrieve(actorName, false);

                        if (actor == null) {
                              actor = playerStorage.getConsole();
                        }

                        IpBanData ban = new IpBanData(IPUtils.toLong(ip), actor, reason, created, expires);

                        plugin.getIpBanStorage().create(ban);
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }
      }

      private void convertKicksTable(DatabaseConnection connection) {
            HashMap<String, PlayerBan> toLookup = new HashMap<String, PlayerBan>();
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT kicked, kicked_by, kick_reason, kick_time FROM " + conversionDb.getTableName("kicksTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }

            try {
                  while (result.next()) {
                        String name = result.getString(0);
                        String actorName = result.getString(1);
                        String reason = result.getString(2);
                        long created = result.getLong(3);

                        PlayerData player = playerStorage.retrieve(name, false);
                        PlayerData actor = playerStorage.retrieve(actorName, false);

                        if (actor == null) {
                              actor = playerStorage.getConsole();
                        }

                        if (player == null) {
                              // Reuse classes for ease, and stop polluting with a class
                              // for each data type
                              toLookup.put(name.toLowerCase(), new PlayerBan(name, actor, reason, created, 0));

                              continue;
                        }

                        PlayerKickData kick = new PlayerKickData(player, actor, reason, created);

                        plugin.getPlayerKickStorage().create(kick);
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }

            if (toLookup.size() == 0) {
                  return;
            }

            try {
                  // Don't want to hit the api limit!
                  Thread.sleep(500L);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            Map<String, UUID> uuids = null;
            try {
                  uuids = new UUIDUtils(toLookup.keySet(), true).call();
            } catch (Exception e) {
                  e.printStackTrace();
                  return;
            }

            for (Entry<String, UUID> entry : uuids.entrySet()) {
                  PlayerBan ban = toLookup.remove(entry.getKey().toLowerCase());

                  ban.setName(entry.getKey());

                  try {
                        PlayerData player = new PlayerData(entry.getValue(), ban.getName());
                        playerStorage.create(player);
                        plugin.getPlayerKickStorage().create(new PlayerKickData(player, ban.getActor(), ban.getReason(), ban.getCreated()));
                  } catch (SQLException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe(ban.getName() + " kick creation failed");
                  }
            }

            if (toLookup.size() != 0) {
                  for (String name : toLookup.keySet()) {
                        plugin.getLogger().severe(ChatColor.RED + name + " lookup failed");
                  }

                  toLookup.clear();
            }
      }

      private void convertWarningsTable(DatabaseConnection connection) {
            HashMap<String, PlayerBan> toLookup = new HashMap<String, PlayerBan>();
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT warned, warned_by, warn_reason, warn_time FROM " + conversionDb.getTableName("warningsTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }

            try {
                  while (result.next()) {
                        String name = result.getString(0);
                        String actorName = result.getString(1);
                        String reason = result.getString(2);
                        long created = result.getLong(3);

                        PlayerData player = playerStorage.retrieve(name, false);
                        PlayerData actor = playerStorage.retrieve(actorName, false);

                        if (actor == null) {
                              actor = playerStorage.getConsole();
                        }

                        if (player == null) {
                              // Reuse classes for ease, and stop polluting with a class
                              // for each data type
                              toLookup.put(name.toLowerCase(), new PlayerBan(name, actor, reason, created, 0));

                              continue;
                        }

                        PlayerWarnData warn = new PlayerWarnData(player, actor, reason, true, created);

                        plugin.getPlayerWarnStorage().create(warn);
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }

            if (toLookup.size() == 0) {
                  return;
            }

            try {
                  // Don't want to hit the api limit!
                  Thread.sleep(500L);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            Map<String, UUID> uuids = null;
            try {
                  uuids = new UUIDUtils(toLookup.keySet(), true).call();
            } catch (Exception e) {
                  e.printStackTrace();
                  return;
            }

            for (Entry<String, UUID> entry : uuids.entrySet()) {
                  PlayerBan ban = toLookup.remove(entry.getKey().toLowerCase());

                  ban.setName(entry.getKey());

                  try {
                        PlayerData player = new PlayerData(entry.getValue(), ban.getName());
                        playerStorage.create(player);
                        plugin.getPlayerWarnStorage().create(new PlayerWarnData(player, ban.getActor(), ban.getReason(), true, ban.getCreated()));
                  } catch (SQLException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe(ban.getName() + "warning creation failed");
                  }
            }

            if (toLookup.size() != 0) {
                  for (String name : toLookup.keySet()) {
                        plugin.getLogger().severe(ChatColor.RED + name + " lookup failed");
                  }

                  toLookup.clear();
            }
      }

      private void convertMuteRecordsTable(DatabaseConnection connection) {
            HashMap<String, PlayerRecordBan> toLookup = new HashMap<String, PlayerRecordBan>();
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT muted, muted_by, mute_reason, mute_time, mute_expired_on, unmuted_by, unmuted_time FROM " + conversionDb.getTableName("mutesRecordTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }

            try {
                  while (result.next()) {
                        String name = result.getString(0);
                        String actorName = result.getString(1);
                        String reason = result.getString(2);
                        long pastCreated = result.getLong(3);
                        long expires = result.getLong(4);
                        String unbannedActorName = result.getString(5);
                        long created = result.getLong(6);

                        PlayerData player = playerStorage.retrieve(name, false);
                        PlayerData banActor = playerStorage.retrieve(actorName, false);
                        PlayerData actor = playerStorage.retrieve(unbannedActorName, false);

                        if (actor == null) {
                              actor = playerStorage.getConsole();
                        }

                        if (banActor == null) {
                              banActor = playerStorage.getConsole();
                        }

                        if (player == null) {
                              toLookup.put(name.toLowerCase(), new PlayerRecordBan(name, banActor, reason, pastCreated, expires, actor, created));

                              continue;
                        }

                        PlayerMuteData ban = new PlayerMuteData(player, banActor, reason, pastCreated, expires);
                        PlayerMuteRecord record = new PlayerMuteRecord(ban, actor);

                        plugin.getPlayerMuteRecordStorage().create(record);
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }

            if (toLookup.size() == 0) {
                  return;
            }

            try {
                  // Don't want to hit the api limit!
                  Thread.sleep(500L);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            Map<String, UUID> uuids = null;
            try {
                  uuids = new UUIDUtils(toLookup.keySet(), true).call();
            } catch (Exception e) {
                  e.printStackTrace();
                  return;
            }

            for (Entry<String, UUID> entry : uuids.entrySet()) {
                  PlayerRecordBan record = toLookup.remove(entry.getKey().toLowerCase());

                  record.setName(entry.getKey());

                  try {
                        PlayerData player = new PlayerData(entry.getValue(), record.getName());
                        playerStorage.create(player);
                        plugin.getPlayerMuteRecordStorage().create(new PlayerMuteRecord(new PlayerMuteData(player, record.getBanActor(), record.getReason(), record.getExpires(), record.getPastCreated()), record.getUnbannedActor(), record.getCreated()));
                  } catch (SQLException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe(record.getName() + " mute record creation failed");
                  }
            }

            if (toLookup.size() != 0) {
                  for (String name : toLookup.keySet()) {
                        plugin.getLogger().severe(ChatColor.RED + name + " lookup failed");
                  }

                  toLookup.clear();
            }
      }

      private void convertMutesTable(DatabaseConnection connection) {
            HashMap<String, PlayerBan> toLookup = new HashMap<String, PlayerBan>();
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT muted, muted_by, mute_reason, mute_time, mute_expires_on FROM " + conversionDb.getTableName("mutesTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }

            try {
                  while (result.next()) {
                        String name = result.getString(0);
                        String actorName = result.getString(1);
                        String reason = result.getString(2);
                        long created = result.getLong(3);
                        long expires = result.getLong(4);

                        PlayerData player = playerStorage.retrieve(name, false);
                        PlayerData actor = playerStorage.retrieve(actorName, false);

                        if (actor == null) {
                              actor = playerStorage.getConsole();
                        }

                        if (player == null) {
                              toLookup.put(name.toLowerCase(), new PlayerBan(name, actor, reason, created, expires));

                              continue;
                        }

                        PlayerMuteData mute = new PlayerMuteData(player, actor, reason, created, expires);

                        plugin.getPlayerMuteStorage().create(mute);
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }

            if (toLookup.size() == 0) {
                  return;
            }

            try {
                  // Don't want to hit the api limit!
                  Thread.sleep(500L);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            Map<String, UUID> uuids = null;
            try {
                  uuids = new UUIDUtils(toLookup.keySet(), true).call();
            } catch (Exception e) {
                  e.printStackTrace();
                  return;
            }

            for (Entry<String, UUID> entry : uuids.entrySet()) {
                  PlayerBan ban = toLookup.remove(entry.getKey().toLowerCase());

                  ban.setName(entry.getKey());

                  try {
                        PlayerData player = new PlayerData(entry.getValue(), ban.getName());
                        playerStorage.create(player);
                        plugin.getPlayerBanStorage().create(new PlayerBanData(player, ban.getActor(), ban.getReason(), ban.getExpires(), ban.getCreated()));
                  } catch (SQLException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe(ban.getName() + "ban creation failed");
                  }
            }

            if (toLookup.size() != 0) {
                  for (String name : toLookup.keySet()) {
                        plugin.getLogger().severe(ChatColor.RED + name + " lookup failed");
                  }

                  toLookup.clear();
            }
      }

      private void convertBanRecordsTable(DatabaseConnection connection) {
            HashMap<String, PlayerRecordBan> toLookup = new HashMap<String, PlayerRecordBan>();
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time FROM " + conversionDb.getTableName("bansRecordTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }

            try {
                  while (result.next()) {
                        String name = result.getString(0);
                        String actorName = result.getString(1);
                        String reason = result.getString(2);
                        long pastCreated = result.getLong(3);
                        long expires = result.getLong(4);
                        String unbannedActorName = result.getString(5);
                        long created = result.getLong(6);

                        PlayerData player = playerStorage.retrieve(name, false);
                        PlayerData banActor = playerStorage.retrieve(actorName, false);
                        PlayerData actor = playerStorage.retrieve(unbannedActorName, false);

                        if (actor == null) {
                              actor = playerStorage.getConsole();
                        }

                        if (banActor == null) {
                              banActor = playerStorage.getConsole();
                        }

                        if (player == null) {
                              toLookup.put(name.toLowerCase(), new PlayerRecordBan(name, banActor, reason, pastCreated, expires, actor, created));

                              continue;
                        }

                        PlayerBanData ban = new PlayerBanData(player, banActor, reason, pastCreated, expires);
                        PlayerBanRecord record = new PlayerBanRecord(ban, actor);

                        plugin.getPlayerBanRecordStorage().create(record);
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }

            if (toLookup.size() == 0) {
                  return;
            }

            try {
                  // Don't want to hit the api limit!
                  Thread.sleep(500L);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            Map<String, UUID> uuids = null;
            try {
                  uuids = new UUIDUtils(toLookup.keySet(), true).call();
            } catch (Exception e) {
                  e.printStackTrace();
                  return;
            }

            for (Entry<String, UUID> entry : uuids.entrySet()) {
                  PlayerRecordBan record = toLookup.remove(entry.getKey().toLowerCase());

                  record.setName(entry.getKey());

                  try {
                        PlayerData player = new PlayerData(entry.getValue(), record.getName());
                        playerStorage.create(player);
                        plugin.getPlayerBanRecordStorage().create(new PlayerBanRecord(new PlayerBanData(player, record.getBanActor(), record.getReason(), record.getExpires(), record.getPastCreated()), record.getUnbannedActor(), record.getCreated()));
                  } catch (SQLException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe(record.getName() + "ban record creation failed");
                  }
            }

            if (toLookup.size() != 0) {
                  for (String name : toLookup.keySet()) {
                        plugin.getLogger().severe(ChatColor.RED + name + " lookup failed");
                  }

                  toLookup.clear();
            }
      }

      private void convertBansTable(DatabaseConnection connection) {
            HashMap<String, PlayerBan> toLookup = new HashMap<String, PlayerBan>();
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT banned, banned_by, ban_reason, ban_time, ban_expires_on FROM " + conversionDb.getTableName("bansTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }

            try {
                  while (result.next()) {
                        String name = result.getString(0);
                        String actorName = result.getString(1);
                        String reason = result.getString(2);
                        long created = result.getLong(3);
                        long expires = result.getLong(4);

                        PlayerData player = playerStorage.retrieve(name, false);
                        PlayerData actor = playerStorage.retrieve(actorName, false);

                        if (actor == null) {
                              actor = playerStorage.getConsole();
                        }

                        if (player == null) {
                              toLookup.put(name.toLowerCase(), new PlayerBan(name, actor, reason, created, expires));

                              continue;
                        }

                        PlayerBanData ban = new PlayerBanData(player, actor, reason, created, expires);

                        plugin.getPlayerBanStorage().create(ban);
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }

            if (toLookup.size() == 0) {
                  return;
            }

            try {
                  // Don't want to hit the api limit!
                  Thread.sleep(500L);
            } catch (InterruptedException e) {
                  e.printStackTrace();
            }

            Map<String, UUID> uuids = null;
            try {
                  uuids = new UUIDUtils(toLookup.keySet(), true).call();
            } catch (Exception e) {
                  e.printStackTrace();
                  return;
            }

            for (Entry<String, UUID> entry : uuids.entrySet()) {
                  PlayerBan ban = toLookup.remove(entry.getKey().toLowerCase());

                  ban.setName(entry.getKey());

                  try {
                        PlayerData player = new PlayerData(entry.getValue(), ban.getName());
                        playerStorage.create(player);
                        plugin.getPlayerBanStorage().create(new PlayerBanData(player, ban.getActor(), ban.getReason(), ban.getExpires(), ban.getCreated()));
                  } catch (SQLException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe(ban.getName() + "ban creation failed");
                  }
            }

            if (toLookup.size() != 0) {
                  for (String name : toLookup.keySet()) {
                        plugin.getLogger().severe(ChatColor.RED + name + " lookup failed");
                  }

                  toLookup.clear();
            }
      }

      private void convertPlayerIpTable(DatabaseConnection connection) {
            DatabaseResults result;
            try {
                  result = connection.compileStatement("SELECT player, ip, last_seen FROM " + conversionDb.getTableName("playerIpsTable"), StatementType.SELECT, null, DatabaseConnection.DEFAULT_RESULT_FLAGS).runQuery(null);
            } catch (SQLException e) {
                  e.printStackTrace();
                  return;
            }
            ArrayList<String> names = new ArrayList<String>(100);
            HashMap<String, PlayerProfile> profiles = new HashMap<String, PlayerProfile>(100);

            try {
                  while (result.next()) {
                        String name = result.getString(0);
                        long ip = result.getLong(1);
                        long lastSeen = result.getLong(2);

                        names.add(name);
                        profiles.put(name.toLowerCase(), new PlayerProfile(name, ip, lastSeen));

                        if (names.size() == 100) {
                              convertProfiles(profiles);
                        }
                  }
            } catch (SQLException e) {
                  e.printStackTrace();
            } finally {
                  result.closeQuietly();
            }
      }

      private void convertProfiles(HashMap<String, PlayerProfile> profiles) {
            Map<String, UUID> uuids = null;
            try {
                  uuids = new UUIDUtils(profiles.keySet(), true).call();
            } catch (Exception e) {
                  e.printStackTrace();
                  return;
            }

            for (Entry<String, UUID> entry : uuids.entrySet()) {
                  PlayerProfile profile = profiles.remove(entry.getKey().toLowerCase());

                  profile.setName(entry.getKey());

                  try {
                        playerStorage.create(new PlayerData(entry.getValue(), profile.getName(), profile.getIp(), profile.getLastSeen()));
                  } catch (SQLException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe(profile.getName() + " creation failed");
                  }
            }

            if (profiles.size() != 0) {
                  for (String name : profiles.keySet()) {
                        plugin.getLogger().severe(ChatColor.RED + name + " lookup failed");
                  }

                  profiles.clear();
            }
      }

}
