package me.confuser.banmanager.listeners;

import com.google.common.collect.ImmutableMap;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.ActionCommand;
import me.confuser.banmanager.configs.Hook;
import me.confuser.banmanager.configs.HooksConfig;
import me.confuser.banmanager.events.*;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.listeners.Listeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;
import java.util.Map;

public class HookListener extends Listeners<BanManager> {

  // TODO refactor to reduce duplicate code
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final PlayerBanEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("ban") : config.getHook("tempban");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "player", event.getBan().getPlayer().getName()
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final PlayerBannedEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("ban") : config.getHook("tempban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getBan().getPlayer().getName()
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnban(final PlayerUnbanEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = config.getHook("unban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "player", event.getBan().getPlayer().getName()
              , "actor", event.getActor().getName()
              , "reason", event.getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMute(final PlayerMuteEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getMute().getExpires() == 0 ? config.getHook("mute") : config.getHook("tempmute");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "player", event.getMute().getPlayer().getName()
              , "actor", event.getMute().getActor().getName()
              , "reason", event.getMute().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMute(final PlayerMutedEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getMute().getExpires() == 0 ? config.getHook("mute") : config.getHook("tempmute");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getMute().getPlayer().getName()
              , "actor", event.getMute().getActor().getName()
              , "reason", event.getMute().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnmute(final PlayerUnmuteEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = config.getHook("unmute");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "player", event.getMute().getPlayer().getName()
              , "actor", event.getActor().getName()
              , "reason", event.getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpBanEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("ipban") : config.getHook("tempipban");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "ip", IPUtils.toString(event.getBan().getIp())
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpBannedEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("ipban") : config.getHook("tempipban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "ip", IPUtils.toString(event.getBan().getIp())
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnban(final IpUnbanEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = config.getHook("unbanip");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "ip", IPUtils.toString(event.getBan().getIp())
              , "actor", event.getActor().getName()
              , "reason", event.getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpRangeBanEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("iprangeban") : config
            .getHook("temprangeipban");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "from", IPUtils.toString(event.getBan().getFromIp())
              , "to", IPUtils.toString(event.getBan().getToIp())
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBan(final IpRangeBannedEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getBan().getExpires() == 0 ? config.getHook("iprangeban") : config
            .getHook("temprangeipban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "from", IPUtils.toString(event.getBan().getFromIp())
              , "to", IPUtils.toString(event.getBan().getToIp())
              , "actor", event.getBan().getActor().getName()
              , "reason", event.getBan().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnban(final IpRangeUnbanEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = config.getHook("unbaniprange");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "from", IPUtils.toString(event.getBan().getFromIp())
              , "to", IPUtils.toString(event.getBan().getToIp())
              , "actor", event.getActor().getName()
              , "reason", event.getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWarn(final PlayerWarnEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = config.getHook("warn");

    if (hook == null) return;

    if (hook.getPre().size() != 0) {
      executeCommands(hook.getPre(), ImmutableMap.of(
              "player", event.getWarning().getPlayer().getName()
              , "actor", event.getWarning().getActor().getName()
              , "reason", event.getWarning().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWarn(final PlayerWarnedEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = event.getWarning().getExpires() == 0 ? config.getHook("warn") : config.getHook("tempwarn");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getWarning().getPlayer().getName()
              , "actor", event.getWarning().getActor().getName()
              , "reason", event.getWarning().getReason()
      ));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onNote(final PlayerNoteCreatedEvent event) {
    HooksConfig config = plugin.getConfiguration().getHooksConfig();
    final Hook hook = config.getHook("note");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
              "player", event.getNote().getPlayer().getName()
              , "actor", event.getNote().getActor().getName()
              , "message", event.getNote().getMessage()
      ));
    }
  }

  private void executeCommands(List<ActionCommand> commands, final Map<String, String> messages) {
    for (final ActionCommand command : commands) {
      plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

        @Override
        public void run() {
          String hookCommand = command.getCommand();

          for (Map.Entry<String, String> entry : messages.entrySet()) {
            hookCommand = hookCommand.replace("[" + entry.getKey() + "]", entry.getValue());
          }

          plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), hookCommand);
        }

      }, command.getDelay());
    }
  }
}
