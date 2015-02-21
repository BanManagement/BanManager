package me.confuser.banmanager.storage.conversion;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.storage.conversion.converters.*;
import org.bukkit.ChatColor;

import java.sql.SQLException;

// This class is horrifically big, lots of repetition, but speed for release over readability :(
// This class will be removed in upcoming releases
public class UUIDConvert {

  private BanManager plugin = BanManager.getPlugin();
  private JdbcPooledConnectionSource conversionConn;

  public UUIDConvert(JdbcPooledConnectionSource conversionConn) {
    this.conversionConn = conversionConn;

    executeConverter(new PlayerIpConverter());

    plugin.getLogger().info(ChatColor.GREEN + "Player ips table converted!");
    plugin.getLogger().info("Starting bans table conversion");

    // Convert bans!
    executeConverter(new BanConverter());

    plugin.getLogger().info(ChatColor.GREEN + "bans table converted!");
    plugin.getLogger().info("Starting ban records table conversion");

    // Convert ban records
    executeConverter(new BanRecordConverter());

    plugin.getLogger().info(ChatColor.GREEN + "Player ban records table converted!");
    plugin.getLogger().info("Starting mutes table conversion");

    // Convert mutes!
    executeConverter(new MuteConverter());

    plugin.getLogger().info(ChatColor.GREEN + "mutes table converted!");
    plugin.getLogger().info("Starting mute records table conversion");

    // Convert ban records
    executeConverter(new MuteRecordConverter());

    plugin.getLogger().info(ChatColor.GREEN + "Mute records table converted!");
    plugin.getLogger().info("Starting warnings table conversion");

    executeConverter(new WarningConverter());

    plugin.getLogger().info(ChatColor.GREEN + "Player warning records table converted!");
    plugin.getLogger().info("Starting kicks table conversion");

    executeConverter(new KickConverter());

    plugin.getLogger().info(ChatColor.GREEN + "Player kicks table converted!");
    plugin.getLogger().info("Starting ip bans table conversion");

    // Convert mutes!
    executeConverter(new IpBanConverter());

    plugin.getLogger().info(ChatColor.GREEN + "ip bans table converted!");
    plugin.getLogger().info("Starting ip bans records table conversion");

    // Convert ban records
    executeConverter(new IpBanRecordConverter());

    plugin.getLogger().info(ChatColor.GREEN + "Ip ban records table converted!");
    plugin.getLogger()
          .info(ChatColor.GREEN + "Conversion complete! Please check logs for errors. Restart the server for new data to take affect!");

    plugin.getConfiguration().conf.set("databases.convert.enabled", false);
    plugin.getConfiguration().save();
  }

  private DatabaseConnection getReadOnly() {
    DatabaseConnection connection;
    try {
      connection = conversionConn.getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
      return null;
    }

    return connection;
  }

  private void executeConverter(Converter converter) {
    DatabaseConnection connection;
    if ((connection = getReadOnly()) == null) {
      plugin.getLogger().severe("Conversion connection failed, aborting conversion!");
      return;
    }

    converter.run(connection);

    try {
      conversionConn.releaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try {
      Thread.sleep(2000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
