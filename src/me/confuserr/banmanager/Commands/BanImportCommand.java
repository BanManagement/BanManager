package me.confuserr.banmanager.Commands;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;
import me.confuserr.banmanager.data.BanData;
import me.confuserr.banmanager.data.IPBanData;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanImportCommand implements CommandExecutor {

	private BanManager plugin;
	public static boolean importInProgress = false;

	public BanImportCommand(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 2)
			return false;
		else if (!args[0].equals("player") && !args[0].equals("ip"))
			return false;
		else if (BanImportCommand.importInProgress) {
			Util.sendMessage(sender, plugin.getMessage("importInProgressError"));
			return true;
		}

		Player player = null;
		String playerName = "Console";

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.import")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		String type = args[0];
		final String reason = Util.getReason(args, 1);

		final String consoleName = playerName;

		BanImportCommand.importInProgress = true;

		if (type.equals("player")) {
			Util.sendMessage(sender, plugin.getMessage("beginingPlayerImport"));

			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					BufferedReader banned;
					try {
						banned = new BufferedReader(new FileReader("banned-players.txt"));
					} catch (FileNotFoundException e) {
						Util.sendMessage(sender, "banned-players.txt not found");
						return;
					}

					Util.sendMessage(sender, plugin.getMessage("scanningDatabase"));

					String nextLine = "";

					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

					ArrayList<BanData> toBan = new ArrayList<BanData>();

					try {
						while ((nextLine = banned.readLine()) != null) {
							if (nextLine.contains("#"))
								continue;
							else {
								String[] details = nextLine.split("\\|");
								String pName = details[0].toLowerCase();

								if (details.length < 4)
									continue;
								else if (!Util.isValidPlayerName(pName))
									continue;
								else if (plugin.isPlayerBanned(pName))
									continue;
								else {

									long date = dateFormat.parse(details[1]).getTime() / 1000;
									String by = details[2];
									long expires = 0;
									String pReason = details[4];

									if (by.equals("Server") || by.equals("(Unknown)"))
										by = consoleName;

									if (pReason.equals("Banned by an operator."))
										pReason = reason;

									if (!details[3].equals("Forever"))
										expires = dateFormat.parse(details[3]).getTime() / 1000;

									toBan.add(new BanData(pName, by, pReason, date, expires));
								}
							}
						}
					} catch (IOException e) {
						Util.sendMessage(sender, ChatColor.RED + "Error occurred");
						BanImportCommand.importInProgress = false;
						return;
					} catch (ParseException e) {
						Util.sendMessage(sender, ChatColor.RED + "Error occurred");
						BanImportCommand.importInProgress = false;
						return;
					}


					Util.sendMessage(sender, plugin.getMessage("scanPlayersFound").replace("[found]", Integer.toString(toBan.size())));

					if (toBan.size() == 0) {
						Util.sendMessage(sender, plugin.getMessage("noPlayersImport"));
						BanImportCommand.importInProgress = false;
						return;
					} else {

						int done = 0;
						double percent = 0;
						double newPercent;
						int totalPlayers = toBan.size();

						for (BanData p : toBan) {
							plugin.addPlayerBan(p);

							done++;

							newPercent = Math.round((double) (done * 100) / totalPlayers);
							if (newPercent != percent) {
								percent = newPercent;
								if (percent % 10 == 0 && (int) percent != 100)
									Util.sendMessage(sender, plugin.getMessage("percentagePlayersImported").replace("[percent]", Double.toString(percent)));
							}
						}

						toBan.clear();

						Util.sendMessage(sender, plugin.getMessage("playerImportComplete"));

						BanImportCommand.importInProgress = false;
					}
				}
			});
		} else if (type.equals("ip")) {
			Util.sendMessage(sender, plugin.getMessage("beginingIpImport"));

			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					BufferedReader banned;
					try {
						banned = new BufferedReader(new FileReader("banned-ips.txt"));
					} catch (FileNotFoundException e) {
						Util.sendMessage(sender, "banned-ips.txt not found");
						return;
					}

					Util.sendMessage(sender, plugin.getMessage("scanningDatabase"));

					String nextLine = "";

					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

					ArrayList<IPBanData> toBan = new ArrayList<IPBanData>();

					try {
						while ((nextLine = banned.readLine()) != null) {
							if (nextLine.contains("#"))
								continue;
							else {
								String[] details = nextLine.split("\\|");

								if (details.length < 4)
									continue;
								else if (!Util.ValidateIPAddress(details[0]))
									continue;
								else if (plugin.isIPBanned(details[0]))
									continue;
								else {

									String pName = details[0];
									long date = dateFormat.parse(details[1]).getTime() / 1000;
									String by = details[2];
									long expires = 0;
									String pReason = details[4];

									if (by.equals("Server") || by.equals("(Unknown)"))
										by = consoleName;

									if (pReason.equals("Banned by an operator."))
										pReason = reason;

									if (!details[3].equals("Forever"))
										expires = dateFormat.parse(details[3]).getTime() / 1000;

									toBan.add(new IPBanData(pName, pReason, by, date, expires));
								}
							}
						}
					} catch (IOException e) {
						Util.sendMessage(sender, ChatColor.RED + "Error occurred");
						BanImportCommand.importInProgress = false;
						return;
					} catch (ParseException e) {
						Util.sendMessage(sender, ChatColor.RED + "Error occurred");
						BanImportCommand.importInProgress = false;
						return;
					}

					Util.sendMessage(sender, plugin.getMessage("scanPlayersFound").replace("[found]", Integer.toString(toBan.size())));

					if (toBan.size() == 0) {
						Util.sendMessage(sender, plugin.getMessage("noIpsImport"));
						BanImportCommand.importInProgress = false;
						return;
					} else {

						int done = 0;
						double percent = 0;
						double newPercent;
						int totalPlayers = toBan.size();

						for (IPBanData p : toBan) {
							plugin.dbLogger.logIpBan(p.getBanned(), p.getBy(), p.getReason(), p.getTime(), p.getExpires());

							done++;

							newPercent = Math.round((double) (done * 100) / totalPlayers);
							if (newPercent != percent) {
								percent = newPercent;
								if (percent % 10 == 0 && (int) percent != 100)
									Util.sendMessage(sender, plugin.getMessage("percentageIpsImported").replace("[percent]", Double.toString(percent)));
							}
						}

						toBan.clear();

						Util.sendMessage(sender, plugin.getMessage("ipImportComplete"));

						BanImportCommand.importInProgress = false;
					}
				}

			});
		}

		return true;
	}
}
