package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.events.IpMuteEvent;
import me.confuser.banmanager.events.IpMutedEvent;
import me.confuser.banmanager.events.IpUnmutedEvent;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import org.bukkit.Bukkit;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IpMuteStorage extends BaseDaoImpl<IpMuteData, Integer> {

  private BanManager plugin = BanManager.getPlugin();
  private ConcurrentHashMap<Long, IpMuteData> mutes = new ConcurrentHashMap<>();

  public IpMuteStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<IpMuteData>) BanManager.getPlugin().getConfiguration().getLocalDb()
                                                                  .getTable("ipMutes"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }

    CloseableIterator<IpMuteData> itr = iterator();

    while (itr.hasNext()) {
      IpMuteData mute = itr.next();

      mutes.put(mute.getIp(), mute);
    }

    itr.close();

    plugin.getLogger().info("Loaded " + mutes.size() + " ip mutes into memory");
  }

  public ConcurrentHashMap<Long, IpMuteData> getMutes() {
    return mutes;
  }

  public boolean isMuted(long ip) {
    return mutes.get(ip) != null;
  }

  public boolean isMuted(InetAddress address) {
    return isMuted(IPUtils.toLong(address));
  }

  public IpMuteData retrieveMute(long ip) throws SQLException {
    List<IpMuteData> mutes = queryForEq("ip", ip);

    if (mutes.isEmpty()) return null;

    return mutes.get(0);
  }

  public IpMuteData getMute(long ip) {
    return mutes.get(ip);
  }

  public IpMuteData getMute(InetAddress address) {
    return getMute(IPUtils.toLong(address));
  }

  public void addMute(IpMuteData mute) {
    mutes.put(mute.getIp(), mute);

    if (plugin.getConfiguration().isBroadcastOnSync()) {
      Bukkit.getServer().getPluginManager().callEvent(new IpMutedEvent(mute, false));
    }
  }

  public void removeMute(IpMuteData mute) {
    removeMute(mute.getIp());
  }

  public void removeMute(long ip) {
    mutes.remove(ip);
  }

  public boolean mute(IpMuteData mute, boolean isSilent) throws SQLException {
    IpMuteEvent event = new IpMuteEvent(mute, isSilent);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    create(mute);
    mutes.put(mute.getIp(), mute);

    Bukkit.getServer().getPluginManager().callEvent(new IpMutedEvent(mute, event.isSilent()));

    return true;
  }

  public boolean unmute(IpMuteData mute, PlayerData actor) throws SQLException {
    return unmute(mute, actor, "");
  }

  public boolean unmute(IpMuteData mute, PlayerData actor, String reason) throws SQLException {
    IpUnmutedEvent event = new IpUnmutedEvent(mute, actor, reason);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    delete(mute);
    mutes.remove(mute.getIp());

    plugin.getIpMuteRecordStorage().addRecord(mute, actor, reason);

    return true;
  }

  public CloseableIterator<IpMuteData> findMutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<IpMuteData, Integer> query = queryBuilder();
    Where<IpMuteData, Integer> where = query.where();
    where
            .ge("created", checkTime)
            .or()
            .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

}
