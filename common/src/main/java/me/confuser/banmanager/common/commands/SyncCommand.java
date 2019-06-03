package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.runnables.BmRunnable;

import java.util.List;

public class SyncCommand extends SingleCommand {

  private final String[] localSync = new String[] { "playerBans", "playerMutes", "ipBans", "ipRangeBans", "expiresCheck" };
  private final String[] globalSync = new String[] { "globalPlayerBans", "globalPlayerMutes", "globalPlayerNotes",
          "globalIpBans" };

  public SyncCommand(LocaleManager locale) {
    super(CommandSpec.BMSYNC.localize(locale), "bmsync", CommandPermission.BMSYNC, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() != 1) return CommandResult.INVALID_ARGS;

    final String type = args.get(0);

    if (!type.equals("local") && !type.equals("global")) return CommandResult.INVALID_ARGS;

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      Message.SYNC_PLAYER_STARTED.send(sender, "type", type);

      if (type.equals("local")) {
        handleLocalSync(plugin);
      } else {
        handleGlobalSync(plugin);
      }

      Message.SYNC_PLAYER_FINISHED.send(sender, "type", type);
    });

    return CommandResult.SUCCESS;
  }

  private void handleLocalSync(BanManagerPlugin plugin) {
    for (String aLocalSync : localSync) {
      BmRunnable runner = plugin.getSyncRunner().getRunner(aLocalSync);

      if (runner.isRunning()) continue;

      runner.run();
    }
  }

  private void handleGlobalSync(BanManagerPlugin plugin) {
    if (plugin.getGlobalPlayerBanStorage() == null) return;

    for (String aGlobalSync : globalSync) {
      BmRunnable runner = plugin.getSyncRunner().getRunner(aGlobalSync);

      if (runner.isRunning()) continue;

      runner.run();
    }
  }
}
