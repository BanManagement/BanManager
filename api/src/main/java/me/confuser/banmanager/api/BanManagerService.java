package me.confuser.banmanager.api;

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

/**
 * Root entry point for the BanManager API. Resolve via either
 * {@link BanManager#get()} (works everywhere) or, on Bukkit, the platform's
 * native services manager:
 *
 * <pre>{@code
 * // Portable across Bukkit, Bungee, Velocity, Sponge, Fabric:
 * BanManagerService bm = BanManager.get();
 *
 * // Bukkit also publishes the service via the Bukkit ServicesManager:
 * BanManagerService bm = Bukkit.getServicesManager().load(BanManagerService.class);
 * }</pre>
 *
 * <p>Velocity, Sponge, Bungee and Fabric have no plugin-extensible service
 * manager that fits this use case, so {@link BanManager#get()} is the
 * recommended path on those platforms.</p>
 *
 * <p>Subservices are stable references — fetch them once at startup and reuse.</p>
 */
public interface BanManagerService {

  PlayerService players();

  BanService bans();

  MuteService mutes();

  WarnService warnings();

  IpBanService ipBans();

  IpMuteService ipMutes();

  IpRangeBanService ipRangeBans();

  NameBanService nameBans();

  NoteService notes();

  ReportService reports();

  HistoryService history();

  EventBus events();

  DatabaseAccess database();

  BanManagerScheduler scheduler();

  MigrationService migrations();
}
