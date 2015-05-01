package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SyncCommand extends BukkitCommand<BanManager> {

  public SyncCommand() {
    super("bmsync");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length != 1) return false;

    final String type = args[0];

    if (!type.equals("local") && !type.equals("external")) return false;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        Message.get("sync.player.started").set("type", type).sendTo(sender);

        if (type.equals("local")) {
          handleLocalSync();
        } else {
          handleExternalSync();
        }

        Message.get("sync.player.finished").set("type", type).sendTo(sender);
      }
    });

    return true;
  }

  private void handleLocalSync() {
    if (!plugin.getBanSync().isRunning()) plugin.getBanSync().run();
    if (!plugin.getMuteSync().isRunning()) plugin.getMuteSync().run();
    if (!plugin.getIpSync().isRunning()) plugin.getIpSync().run();
    if (!plugin.getIpRangeSync().isRunning()) plugin.getIpRangeSync().run();
    if (!plugin.getExpiresSync().isRunning()) plugin.getExpiresSync().run();
  }

  private void handleExternalSync() {
    if (plugin.getExternalPlayerBanStorage() == null) return;

    if (!plugin.getExternalBanSync().isRunning()) plugin.getExternalBanSync().run();
    if (!plugin.getExternalMuteSync().isRunning()) plugin.getExternalMuteSync().run();
    if (!plugin.getExternalIpSync().isRunning()) plugin.getExternalIpSync().run();
  }
}
