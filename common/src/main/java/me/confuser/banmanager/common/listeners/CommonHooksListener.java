package me.confuser.banmanager.common.listeners;

import com.google.common.collect.ImmutableMap;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.ActionCommand;
import me.confuser.banmanager.common.configs.Hook;
import me.confuser.banmanager.common.configs.HooksConfig;
import me.confuser.banmanager.common.data.*;

import java.util.List;
import java.util.Map;

public class CommonHooksListener {
  private BanManagerPlugin plugin;

  public CommonHooksListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void onBan(PlayerBanData data, boolean pre) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = data.getExpires() == 0 ? config.getHook("ban") : config.getHook("tempban");

    if (hook == null) return;

    List<ActionCommand> commands = pre ? hook.getPre() : hook.getPost();

    if (commands.size() != 0) {
      executeCommands(commands, ImmutableMap.of(
          "player", data.getPlayer().getName()
          , "playerId", data.getPlayer().getUUID().toString()
          , "actor", data.getActor().getName()
          , "reason", data.getReason()
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onUnban(PlayerBanData data, PlayerData actor, String reason) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unban");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
          "player", data.getPlayer().getName()
          , "playerId", data.getPlayer().getUUID().toString()
          , "actor", actor.getName()
          , "reason", reason
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onMute(PlayerMuteData data, boolean pre) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = data.getExpires() == 0 ? config.getHook("mute") : config.getHook("tempmute");

    if (hook == null) return;

    List<ActionCommand> commands = pre ? hook.getPre() : hook.getPost();

    if (commands.size() != 0) {
      executeCommands(commands, ImmutableMap.of(
          "player", data.getPlayer().getName()
          , "playerId", data.getPlayer().getUUID().toString()
          , "actor", data.getActor().getName()
          , "reason", data.getReason()
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onUnmute(PlayerMuteData data, PlayerData actor, String reason) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unmute");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
          "player", data.getPlayer().getName()
          , "playerId", data.getPlayer().getUUID().toString()
          , "actor", actor.getName()
          , "reason", reason
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onBan(IpBanData data, boolean pre) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = data.getExpires() == 0 ? config.getHook("ipban") : config.getHook("tempipban");

    if (hook == null) return;

    List<ActionCommand> commands = pre ? hook.getPre() : hook.getPost();

    if (commands.size() != 0) {
      executeCommands(commands, ImmutableMap.of(
          "ip", data.getIp().toString()
          , "actor", data.getActor().getName()
          , "reason", data.getReason()
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onUnban(IpBanData data, PlayerData actor, String reason) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unbanip");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
          "ip", data.getIp().toString()
          , "actor", actor.getName()
          , "reason", reason
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onBan(IpRangeBanData data, boolean pre) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = data.getExpires() == 0 ? config.getHook("iprangeban") : config
        .getHook("temprangeipban");

    if (hook == null) return;

    List<ActionCommand> commands = pre ? hook.getPre() : hook.getPost();

    if (commands.size() != 0) {
      executeCommands(commands, ImmutableMap.of(
          "from", data.getFromIp().toString()
          , "to", data.getToIp().toString()
          , "actor", data.getActor().getName()
          , "reason", data.getReason()
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onUnban(IpRangeBanData data, PlayerData actor, String reason) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("unbaniprange");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
          "from", data.getFromIp().toString()
          , "to", data.getToIp().toString()
          , "actor", actor.getName()
          , "reason", reason
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onWarn(PlayerWarnData data, boolean pre) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("warn");

    if (hook == null) return;

    List<ActionCommand> commands = pre ? hook.getPre() : hook.getPost();

    if (commands.size() != 0) {
      executeCommands(commands, ImmutableMap.of(
          "player", data.getPlayer().getName()
          , "playerId", data.getPlayer().getUUID().toString()
          , "actor", data.getActor().getName()
          , "reason", data.getReason()
          , "expires", Long.toString(data.getExpires())
      ));
    }
  }

  public void onNote(PlayerNoteData data) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("note");

    if (hook == null) return;

    if (hook.getPost().size() != 0) {
      executeCommands(hook.getPost(), ImmutableMap.of(
          "player", data.getPlayer().getName()
          , "playerId", data.getPlayer().getUUID().toString()
          , "actor", data.getActor().getName()
          , "message", data.getMessage()
      ));
    }
  }

  public void onReport(PlayerReportData data, boolean pre) {
    HooksConfig config = plugin.getConfig().getHooksConfig();
    final Hook hook = config.getHook("report");

    if (hook == null) return;

    List<ActionCommand> commands = pre ? hook.getPre() : hook.getPost();

    if (hook.getPost().size() != 0) {
      executeCommands(commands, ImmutableMap.of(
          "id", String.valueOf(data.getId()),
          "player", data.getPlayer().getName()
          , "playerId", data.getPlayer().getUUID().toString()
          , "actor", data.getActor().getName()
          , "message", data.getReason()
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
