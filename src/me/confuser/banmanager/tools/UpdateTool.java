package me.confuser.banmanager.tools;

import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.BmTool;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class UpdateTool implements BmTool {

	public String name = "Update";

	@SuppressWarnings("deprecation")
	@Override
	public void run(final CommandSender sender, String[] args) {
		if (sender.hasPermission("bm.tools.update")) {
			if (!BmAPI.getBanManager().checkForUpdates())
				sender.sendMessage(ChatColor.RED + "[BanManager] Please enable update checking within your config!");
			if (BmAPI.getBanManager().isUpdateAvailable()) {
				BmAPI.getBanManager().getServer().getScheduler().scheduleAsyncDelayedTask(BmAPI.getBanManager(), new Runnable() {
					public void run() {
						new Updater(BmAPI.getBanManager(), 41473, BmAPI.getBanManager().jarFile, Updater.UpdateType.NO_VERSION_CHECK, true);

						sender.sendMessage(ChatColor.GREEN + "[BanManager] Updating, please restart the server in a moment to apply the update.");
					}
				});
			} else {
				sender.sendMessage(ChatColor.RED + "No updates available.");
			}
		}
	}
}
