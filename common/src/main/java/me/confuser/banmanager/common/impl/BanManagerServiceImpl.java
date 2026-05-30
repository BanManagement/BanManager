package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.BanManagerService;
import me.confuser.banmanager.api.database.DatabaseAccess;
import me.confuser.banmanager.api.database.MigrationService;
import me.confuser.banmanager.api.event.EventBus;
import me.confuser.banmanager.api.scheduler.BanManagerScheduler;
import me.confuser.banmanager.api.service.BanService;
import me.confuser.banmanager.api.service.HistoryService;
import me.confuser.banmanager.api.service.IpBanService;
import me.confuser.banmanager.api.service.IpMuteService;
import me.confuser.banmanager.api.service.IpRangeBanService;
import me.confuser.banmanager.api.service.MuteService;
import me.confuser.banmanager.api.service.NameBanService;
import me.confuser.banmanager.api.service.NoteService;
import me.confuser.banmanager.api.service.PlayerService;
import me.confuser.banmanager.api.service.ReportService;
import me.confuser.banmanager.api.service.WarnService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.service.BanServiceImpl;
import me.confuser.banmanager.common.impl.service.HistoryServiceImpl;
import me.confuser.banmanager.common.impl.service.IpBanServiceImpl;
import me.confuser.banmanager.common.impl.service.IpMuteServiceImpl;
import me.confuser.banmanager.common.impl.service.IpRangeBanServiceImpl;
import me.confuser.banmanager.common.impl.service.MuteServiceImpl;
import me.confuser.banmanager.common.impl.service.NameBanServiceImpl;
import me.confuser.banmanager.common.impl.service.NoteServiceImpl;
import me.confuser.banmanager.common.impl.service.PlayerServiceImpl;
import me.confuser.banmanager.common.impl.service.ReportServiceImpl;
import me.confuser.banmanager.common.impl.service.WarnServiceImpl;

import java.util.Objects;

/**
 * Composite root of the public {@link BanManagerService} API. Holds the
 * concrete sub-service implementations and the shared infrastructure
 * ({@link EventBus}, {@link DatabaseAccess}, {@link BanManagerScheduler})
 * so platform bootstrap code only needs to register a single object. The
 * {@link AsyncSupport} executor is injected through the constructor and
 * threaded into each sub-service; the composite does not retain a
 * reference itself.
 *
 * <p>Lifecycle: created in
 * {@code BanManagerPlugin.enable()} after storage initialises, registered
 * with the platform service manager (or {@link java.util.ServiceLoader}),
 * shut down in {@code BanManagerPlugin.disable()}.</p>
 */
public final class BanManagerServiceImpl implements BanManagerService {

  private final PlayerService players;
  private final BanService bans;
  private final MuteService mutes;
  private final WarnService warnings;
  private final IpBanService ipBans;
  private final IpMuteService ipMutes;
  private final IpRangeBanService ipRangeBans;
  private final NameBanService nameBans;
  private final NoteService notes;
  private final ReportService reports;
  private final HistoryService history;
  private final EventBus events;
  private final DatabaseAccess database;
  private final BanManagerScheduler scheduler;
  private final MigrationService migrations;

  public BanManagerServiceImpl(BanManagerPlugin plugin,
                               EventBus events,
                               DatabaseAccess database,
                               BanManagerScheduler scheduler,
                               AsyncSupport async) {
    Objects.requireNonNull(plugin, "plugin");
    this.events = Objects.requireNonNull(events, "events");
    this.database = Objects.requireNonNull(database, "database");
    this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    Objects.requireNonNull(async, "async");

    this.players = new PlayerServiceImpl(plugin, async);
    this.bans = new BanServiceImpl(plugin, async);
    this.mutes = new MuteServiceImpl(plugin, async);
    this.warnings = new WarnServiceImpl(plugin, async);
    this.ipBans = new IpBanServiceImpl(plugin, async);
    this.ipMutes = new IpMuteServiceImpl(plugin, async);
    this.ipRangeBans = new IpRangeBanServiceImpl(plugin, async);
    this.nameBans = new NameBanServiceImpl(plugin, async);
    this.notes = new NoteServiceImpl(plugin, async);
    this.reports = new ReportServiceImpl(plugin, async);
    this.history = new HistoryServiceImpl(plugin, async);
    this.migrations = new MigrationServiceImpl(plugin);
  }

  @Override public PlayerService players() { return players; }
  @Override public BanService bans() { return bans; }
  @Override public MuteService mutes() { return mutes; }
  @Override public WarnService warnings() { return warnings; }
  @Override public IpBanService ipBans() { return ipBans; }
  @Override public IpMuteService ipMutes() { return ipMutes; }
  @Override public IpRangeBanService ipRangeBans() { return ipRangeBans; }
  @Override public NameBanService nameBans() { return nameBans; }
  @Override public NoteService notes() { return notes; }
  @Override public ReportService reports() { return reports; }
  @Override public HistoryService history() { return history; }
  @Override public EventBus events() { return events; }
  @Override public DatabaseAccess database() { return database; }
  @Override public BanManagerScheduler scheduler() { return scheduler; }
  @Override public MigrationService migrations() { return migrations; }
}
