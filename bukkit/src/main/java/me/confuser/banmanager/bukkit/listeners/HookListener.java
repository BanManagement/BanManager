package me.confuser.banmanager.bukkit.listeners;

import com.google.common.collect.ImmutableMap;
import me.confuser.banmanager.bukkit.api.events.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.ActionCommand;
import me.confuser.banmanager.common.configs.Hook;
import me.confuser.banmanager.common.configs.HooksConfig;
import me.confuser.banmanager.common.util.IPUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

public class HookListener implements Listener {

  private BanManagerPlugin plugin;

  public HookListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  // TODO refactor to reduce duplicate code
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final PlayerBanEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("ban") : config.getHook("tempban");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "player", event.getBan().getPlayer().getName()
              , "playerId", event.getBan().getPlayer().getUUID().toString()
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final PlayerBannedEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("ban") : config.getHook("tempban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getBan().getPlayer().getName()
              , "playerId", event.getBan().getPlayer().getUUID().toString()
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnban(final PlayerUnbanEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getBan().getPlayer().getName()
              , "playerId", event.getBan().getPlayer().getUUID().toString()
              , "actor", event.getActor().getName()
              , "reason", event.getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMute(final PlayerMuteEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getMute().getExpires() == 0 ? config.getHook("mute") : config.getHook("tempmute");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "player", event.getMute().getPlayer().getName()
              , "playerId", event.getMute().getPlayer().getUUID().toString()
              , "actor", event.getMute().getActor().getName()
              , "reason", event.getMute().getReason()
              , "expires", Long.toString(event.getMute().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMute(final PlayerMutedEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getMute().getExpires() == 0 ? config.getHook("mute") : config.getHook("tempmute");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getMute().getPlayer().getName()
              , "playerId", event.getMute().getPlayer().getUUID().toString()
              , "actor", event.getMute().getActor().getName()
              , "reason", event.getMute().getReason()
              , "expires", Long.toString(event.getMute().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnmute(final PlayerUnmuteEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unmute");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getMute().getPlayer().getName()
              , "playerId", event.getMute().getPlayer().getUUID().toString()
              , "actor", event.getActor().getName()
              , "reason", event.getReason()
              , "expires", Long.toString(event.getMute().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpBanEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("ipban") : config.getHook("tempipban");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "ip", IPUtils.toString(event.getBan().getIp())
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpBannedEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("ipban") : config.getHook("tempipban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "ip", IPUtils.toString(event.getBan().getIp())
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnban(final IpUnbanEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unbanip");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "ip", IPUtils.toString(event.getBan().getIp())
              , "actor", event.getActor().getName()
              , "reason", event.getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpRangeBanEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("iprangeban") : config
            .getHook("temprangeipban");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "from", IPUtils.toString(event.getBan().getFromIp())
              , "to", IPUtils.toString(event.getBan().getToIp())
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpRangeBannedEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("iprangeban") : config
            .getHook("temprangeipban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "from", IPUtils.toString(event.getBan().getFromIp())
              , "to", IPUtils.toString(event.getBan().getToIp())
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnban(final IpRangeUnbanEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unbaniprange");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "from", IPUtils.toString(event.getBan().getFromIp())
              , "to", IPUtils.toString(event.getBan().getToIp())
              , "actor", event.getActor().getName()
              , "reason", event.getReason()
              , "expires", Long.toString(event.getBan().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWarn(final PlayerWarnEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("warn");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "player", event.getWarning().getPlayer().getName()
              , "playerId", event.getWarning().getPlayer().getUUID().toString()
              , "actor", event.getWarning().getActor().getName()
              , "reason", event.getWarning().getReason()
              , "expires", Long.toString(event.getWarning().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWarn(final PlayerWarnedEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = event.getWarning().getExpires() == 0 ? config.getHook("warn") : config.getHook("tempwarn");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getWarning().getPlayer().getName()
              , "playerId", event.getWarning().getPlayer().getUUID().toString()
              , "actor", event.getWarning().getActor().getName()
              , "reason", event.getWarning().getReason()
              , "expires", Long.toString(event.getWarning().getExpires())
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onNote(final PlayerNoteCreatedEvent event) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("note");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getNote().getPlayer().getName()
              , "playerId", event.getNote().getPlayer().getUUID().toString()
              , "actor", event.getNote().getActor().getName()
              , "message", event.getNote().getMessage()
      ));
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
      }, command.getDelay());
    }
  }
}
