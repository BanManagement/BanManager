package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.events.IpBanEvent;
import me.confuser.banmanager.events.IpBannedEvent;
import me.confuser.banmanager.events.IpUnbanEvent;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import org.bukkit.Bukkit;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IpBanStorage extends BaseDaoImpl<IpBanData, Integer> {

  private BanManager plugin = BanManager.getPlugin();
  private ConcurrentHashMap<Long, IpBanData> bans = new ConcurrentHashMap<>();

  public IpBanStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<IpBanData>) BanManager.getPlugin().getConfiguration().getLocalDb()
                                                                 .getTable("ipBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }

    CloseableIterator<IpBanData> itr = iterator();

    while (itr.hasNext()) {
      IpBanData ban = itr.next();

      bans.put(ban.getIp(), ban);
    }

    itr.close();

    plugin.getLogger().info("Loaded " + bans.size() + " ip bans into memory");
  }

  public ConcurrentHashMap<Long, IpBanData> getBans() {
    return bans;
  }

  public boolean isBanned(long ip) {
    return bans.get(ip) != null;
  }

  public boolean isBanned(InetAddress address) {
    return isBanned(IPUtils.toLong(address));
  }

  public IpBanData retrieveBan(long ip) throws SQLException {
    List<IpBanData> bans = queryForEq("ip", ip);

    if (bans.isEmpty()) return null;

    return bans.get(0);
  }

  public IpBanData getBan(long ip) {
    return bans.get(ip);
  }

  public IpBanData getBan(InetAddress address) {
    return getBan(IPUtils.toLong(address));
  }

  public void addBan(IpBanData ban) {
    bans.put(ban.getIp(), ban);

    if (plugin.getConfiguration().isBroadcastOnSync()) {
      Bukkit.getServer().getPluginManager().callEvent(new IpBannedEvent(ban, false));
    }
  }

  public void removeBan(IpBanData ban) {
    removeBan(ban.getIp());
  }

  public void removeBan(long ip) {
    bans.remove(ip);
  }

  public boolean ban(IpBanData ban, boolean isSilent) throws SQLException {
    IpBanEvent event = new IpBanEvent(ban, isSilent);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    create(ban);
    bans.put(ban.getIp(), ban);

    Bukkit.getServer().getPluginManager().callEvent(new IpBannedEvent(ban, event.isSilent()));

    return true;
  }

  public boolean unban(IpBanData ban, PlayerData actor) throws SQLException {
    return unban(ban, actor, "");
  }

  public boolean unban(IpBanData ban, PlayerData actor, String reason) throws SQLException {
    IpUnbanEvent event = new IpUnbanEvent(ban, actor, reason);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    delete(ban);
    bans.remove(ban.getIp());

    plugin.getIpBanRecordStorage().addRecord(ban, actor, reason);

    return true;
  }

  public CloseableIterator<IpBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<IpBanData, Integer> query = queryBuilder();
    Where<IpBanData, Integer> where = query.where();
    where
            .ge("created", checkTime)
            .or()
            .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

}
