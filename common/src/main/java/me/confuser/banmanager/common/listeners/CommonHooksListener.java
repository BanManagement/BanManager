package me.confuser.banmanager.common.listeners;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.dto.PlayerNote;
import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.dto.PlayerWarn;
import me.confuser.banmanager.api.event.ip.IpBanEvent;
import me.confuser.banmanager.api.event.ip.IpBannedEvent;
import me.confuser.banmanager.api.event.ip.IpRangeBanEvent;
import me.confuser.banmanager.api.event.ip.IpRangeBannedEvent;
import me.confuser.banmanager.api.event.ip.IpRangeUnbannedEvent;
import me.confuser.banmanager.api.event.ip.IpUnbannedEvent;
import me.confuser.banmanager.api.event.player.PlayerBanEvent;
import me.confuser.banmanager.api.event.player.PlayerBannedEvent;
import me.confuser.banmanager.api.event.player.PlayerMuteEvent;
import me.confuser.banmanager.api.event.player.PlayerMutedEvent;
import me.confuser.banmanager.api.event.player.PlayerNoteCreatedEvent;
import me.confuser.banmanager.api.event.player.PlayerReportEvent;
import me.confuser.banmanager.api.event.player.PlayerReportedEvent;
import me.confuser.banmanager.api.event.player.PlayerUnbannedEvent;
import me.confuser.banmanager.api.event.player.PlayerUnmutedEvent;
import me.confuser.banmanager.api.event.player.PlayerWarnEvent;
import me.confuser.banmanager.api.event.player.PlayerWarnedEvent;
import me.confuser.banmanager.api.request.BanRequest;
import me.confuser.banmanager.api.request.IpBanRequest;
import me.confuser.banmanager.api.request.IpRangeBanRequest;
import me.confuser.banmanager.api.request.MuteRequest;
import me.confuser.banmanager.api.request.ReportRequest;
import me.confuser.banmanager.api.request.WarnRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.ActionCommand;
import me.confuser.banmanager.common.configs.Hook;
import me.confuser.banmanager.common.configs.HooksConfig;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.google.guava.collect.ImmutableMap;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Subscribes to ban/mute/warn/etc. events and runs configured hook commands
 * (pre and post). Pre-event commands run on cancellable events at
 * {@link me.confuser.banmanager.api.event.EventPriority#MONITOR} priority so
 * they observe the final post-cancellation state.
 */
public class CommonHooksListener {

  private final BanManagerPlugin plugin;

  public CommonHooksListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    subscribe();
  }

  private void subscribe() {
    plugin.getEventBus().subscribe(PlayerBanEvent.class, this::onPlayerBanPre);
    plugin.getEventBus().subscribe(PlayerBannedEvent.class, e -> onBan(e.ban(), false, e.silent()));
    plugin.getEventBus().subscribe(PlayerUnbannedEvent.class,
        e -> onUnban(e.ban(), e.actor(), e.reason(), e.silent()));

    plugin.getEventBus().subscribe(PlayerMuteEvent.class, this::onPlayerMutePre);
    plugin.getEventBus().subscribe(PlayerMutedEvent.class, e -> onMute(e.mute(), false, e.silent()));
    plugin.getEventBus().subscribe(PlayerUnmutedEvent.class,
        e -> onUnmute(e.mute(), e.actor(), e.reason(), e.silent()));

    plugin.getEventBus().subscribe(IpBanEvent.class, this::onIpBanPre);
    plugin.getEventBus().subscribe(IpBannedEvent.class, e -> onBan(e.ban(), false, e.silent()));
    plugin.getEventBus().subscribe(IpUnbannedEvent.class,
        e -> onUnban(e.ban(), e.actor(), e.reason(), e.silent()));

    plugin.getEventBus().subscribe(IpRangeBanEvent.class, this::onIpRangeBanPre);
    plugin.getEventBus().subscribe(IpRangeBannedEvent.class, e -> onBan(e.ban(), false, e.silent()));
    plugin.getEventBus().subscribe(IpRangeUnbannedEvent.class,
        e -> onUnban(e.ban(), e.actor(), e.reason(), e.silent()));

    plugin.getEventBus().subscribe(PlayerWarnEvent.class, this::onPlayerWarnPre);
    plugin.getEventBus().subscribe(PlayerWarnedEvent.class, e -> onWarn(e.warn(), false, e.silent()));

    plugin.getEventBus().subscribe(PlayerNoteCreatedEvent.class, e -> onNote(e.note(), false));

    plugin.getEventBus().subscribe(PlayerReportEvent.class, this::onPlayerReportPre);
    plugin.getEventBus().subscribe(PlayerReportedEvent.class, e -> onReport(e.report(), false, false));
  }

  private void onPlayerBanPre(PlayerBanEvent event) {
    if (event.isCancelled()) return;
    BanRequest request = event.request();
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = request.expires() == 0 ? config.getHook("ban") : config.getHook("tempban");
    if (hook == null || shouldSkip(hook, request.silent()) || hook.pre().isEmpty()) return;

    String playerName = nameOf(request.player());
    String actorName = nameOf(request.actor());
    executeCommands(hook.pre(), ImmutableMap.of(
        "player", playerName,
        "playerId", String.valueOf(request.player()),
        "actor", actorName,
        "reason", request.reason(),
        "expires", Long.toString(request.expires())
    ));
  }

  public void onBan(PlayerBan data, boolean pre, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = data.expires() == 0 ? config.getHook("ban") : config.getHook("tempban");
    if (hook == null || shouldSkip(hook, silent)) return;

    List<ActionCommand> commands = pre ? hook.pre() : hook.post();
    if (commands.isEmpty()) return;

    executeCommands(commands, ImmutableMap.of(
        "player", data.player().name(),
        "playerId", data.player().uuid().toString(),
        "actor", data.actor().name(),
        "reason", data.reason(),
        "expires", Long.toString(data.expires())
    ));
  }

  public void onUnban(PlayerBan data, Player actor, String reason, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unban");
    if (hook == null || shouldSkip(hook, silent) || hook.post().isEmpty()) return;

    executeCommands(hook.post(), ImmutableMap.of(
        "player", data.player().name(),
        "playerId", data.player().uuid().toString(),
        "actor", actor.name(),
        "reason", reason,
        "expires", Long.toString(data.expires())
    ));
  }

  private void onPlayerMutePre(PlayerMuteEvent event) {
    if (event.isCancelled()) return;
    MuteRequest request = event.request();
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = request.expires() == 0 ? config.getHook("mute") : config.getHook("tempmute");
    if (hook == null || shouldSkip(hook, request.silent()) || hook.pre().isEmpty()) return;

    executeCommands(hook.pre(), ImmutableMap.of(
        "player", nameOf(request.player()),
        "playerId", String.valueOf(request.player()),
        "actor", nameOf(request.actor()),
        "reason", request.reason(),
        "expires", Long.toString(request.expires())
    ));
  }

  public void onMute(PlayerMute data, boolean pre, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = data.expires() == 0 ? config.getHook("mute") : config.getHook("tempmute");
    if (hook == null || shouldSkip(hook, silent)) return;

    List<ActionCommand> commands = pre ? hook.pre() : hook.post();
    if (commands.isEmpty()) return;

    executeCommands(commands, ImmutableMap.of(
        "player", data.player().name(),
        "playerId", data.player().uuid().toString(),
        "actor", data.actor().name(),
        "reason", data.reason(),
        "expires", Long.toString(data.expires())
    ));
  }

  public void onUnmute(PlayerMute data, Player actor, String reason, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unmute");
    if (hook == null || shouldSkip(hook, silent) || hook.post().isEmpty()) return;

    executeCommands(hook.post(), ImmutableMap.of(
        "player", data.player().name(),
        "playerId", data.player().uuid().toString(),
        "actor", actor.name(),
        "reason", reason,
        "expires", Long.toString(data.expires())
    ));
  }

  private void onIpBanPre(IpBanEvent event) {
    if (event.isCancelled()) return;
    IpBanRequest request = event.request();
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = request.expires() == 0 ? config.getHook("ipban") : config.getHook("tempipban");
    if (hook == null || shouldSkip(hook, request.silent()) || hook.pre().isEmpty()) return;

    executeCommands(hook.pre(), ImmutableMap.of(
        "ip", String.valueOf(request.ip()),
        "actor", nameOf(request.actor()),
        "reason", request.reason(),
        "expires", Long.toString(request.expires())
    ));
  }

  public void onBan(IpBan data, boolean pre, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = data.expires() == 0 ? config.getHook("ipban") : config.getHook("tempipban");
    if (hook == null || shouldSkip(hook, silent)) return;

    List<ActionCommand> commands = pre ? hook.pre() : hook.post();
    if (commands.isEmpty()) return;

    executeCommands(commands, ImmutableMap.of(
        "ip", data.ip().toString(),
        "actor", data.actor().name(),
        "reason", data.reason(),
        "expires", Long.toString(data.expires())
    ));
  }

  public void onUnban(IpBan data, Player actor, String reason, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unbanip");
    if (hook == null || shouldSkip(hook, silent) || hook.post().isEmpty()) return;

    executeCommands(hook.post(), ImmutableMap.of(
        "ip", data.ip().toString(),
        "actor", actor.name(),
        "reason", reason,
        "expires", Long.toString(data.expires())
    ));
  }

  private void onIpRangeBanPre(IpRangeBanEvent event) {
    if (event.isCancelled()) return;
    IpRangeBanRequest request = event.request();
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = request.expires() == 0 ? config.getHook("iprangeban") : config.getHook("temprangeipban");
    if (hook == null || shouldSkip(hook, request.silent()) || hook.pre().isEmpty()) return;

    IPAddress fromIp = request.fromIp();
    IPAddress toIp = request.toIp();
    executeCommands(hook.pre(), ImmutableMap.of(
        "from", fromIp == null ? "" : fromIp.toString(),
        "to", toIp == null ? "" : toIp.toString(),
        "actor", nameOf(request.actor()),
        "reason", request.reason(),
        "expires", Long.toString(request.expires())
    ));
  }

  public void onBan(IpRangeBan data, boolean pre, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = data.expires() == 0 ? config.getHook("iprangeban") : config.getHook("temprangeipban");
    if (hook == null || shouldSkip(hook, silent)) return;

    List<ActionCommand> commands = pre ? hook.pre() : hook.post();
    if (commands.isEmpty()) return;

    executeCommands(commands, ImmutableMap.of(
        "from", data.fromIp().toString(),
        "to", data.toIp().toString(),
        "actor", data.actor().name(),
        "reason", data.reason(),
        "expires", Long.toString(data.expires())
    ));
  }

  public void onUnban(IpRangeBan data, Player actor, String reason, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unbaniprange");
    if (hook == null || shouldSkip(hook, silent) || hook.post().isEmpty()) return;

    executeCommands(hook.post(), ImmutableMap.of(
        "from", data.fromIp().toString(),
        "to", data.toIp().toString(),
        "actor", actor.name(),
        "reason", reason,
        "expires", Long.toString(data.expires())
    ));
  }

  private void onPlayerWarnPre(PlayerWarnEvent event) {
    if (event.isCancelled()) return;
    WarnRequest request = event.request();
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("warn");
    if (hook == null || shouldSkip(hook, request.silent()) || hook.pre().isEmpty()) return;

    executeCommands(hook.pre(), ImmutableMap.of(
        "player", nameOf(request.player()),
        "playerId", String.valueOf(request.player()),
        "actor", nameOf(request.actor()),
        "reason", request.reason(),
        "expires", Long.toString(request.expires())
    ));
  }

  public void onWarn(PlayerWarn data, boolean pre, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("warn");
    if (hook == null || shouldSkip(hook, silent)) return;

    List<ActionCommand> commands = pre ? hook.pre() : hook.post();
    if (commands.isEmpty()) return;

    executeCommands(commands, ImmutableMap.of(
        "player", data.player().name(),
        "playerId", data.player().uuid().toString(),
        "actor", data.actor().name(),
        "reason", data.reason(),
        "expires", Long.toString(data.expires())
    ));
  }

  public void onNote(PlayerNote data, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("note");
    if (hook == null || shouldSkip(hook, silent) || hook.post().isEmpty()) return;

    executeCommands(hook.post(), ImmutableMap.of(
        "player", data.player().name(),
        "playerId", data.player().uuid().toString(),
        "actor", data.actor().name(),
        "message", data.message()
    ));
  }

  private void onPlayerReportPre(PlayerReportEvent event) {
    if (event.isCancelled()) return;
    ReportRequest request = event.request();
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("report");
    if (hook == null || hook.pre().isEmpty()) return;

    executeCommands(hook.pre(), ImmutableMap.of(
        "id", "0",
        "player", nameOf(request.player()),
        "playerId", String.valueOf(request.player()),
        "actor", nameOf(request.actor()),
        "message", request.reason()
    ));
  }

  public void onReport(PlayerReport data, boolean pre, boolean silent) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("report");
    if (hook == null || shouldSkip(hook, silent)) return;

    List<ActionCommand> commands = pre ? hook.pre() : hook.post();
    if (commands.isEmpty()) return;

    executeCommands(commands, ImmutableMap.of(
        "id", String.valueOf(data.id()),
        "player", data.player().name(),
        "playerId", data.player().uuid().toString(),
        "actor", data.actor().name(),
        "message", data.reason()
    ));
  }

  private boolean shouldSkip(Hook hook, boolean silent) {
    return silent && hook.ignoreSilent();
  }

  private String nameOf(UUID uuid) {
    if (uuid == null) return "";
    try {
      PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
      return data != null ? data.getName() : uuid.toString();
    } catch (SQLException e) {
      return uuid.toString();
    }
  }

  private void executeCommands(List<ActionCommand> commands, final Map<String, String> messages) {
    for (final ActionCommand command : commands) {
      plugin.getScheduler().runSyncLater(() -> {
        String hookCommand = command.getCommand();

        for (Map.Entry<String, String> entry : messages.entrySet()) {
          hookCommand = hookCommand.replace("[" + entry.getKey() + "]", entry.getValue());
        }

        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommand);
      }, Duration.ofMillis(command.getDelay()));
    }
  }
}
