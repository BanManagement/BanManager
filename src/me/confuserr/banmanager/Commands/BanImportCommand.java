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
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 2)
			return false;
		else if (!args[0].equals("player") && !args[0].equals("ip"))
			return false;
		else if (BanImportCommand.importInProgress) {
			Util.sendMessage(sender, plugin.banMessages.get("importInProgressError"));
			return true;
		}

		Player player = null;
		String playerName = "Console";

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.import")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}

		String type = args[0];
		final String reason = Util.getReason(args, 1);

		final String consoleName = playerName;

		BanImportCommand.importInProgress = true;

		if (type.equals("player")) {
			Util.sendMessage(sender, plugin.banMessages.get("beginingPlayerImport"));

			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					BufferedReader banned;
					try {
						banned = new BufferedReader(new FileReader("banned-players.txt"));
					} catch (FileNotFoundException e) {
						Util.sendMessage(sender, "banned-players.txt not found");
						return;
					}

					Util.sendMessage(sender, plugin.banMessages.get("scanningDatabase"));

					String nextLine = "";

					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

					ArrayList<BanData> toBan = new ArrayList<BanData>();

					try {
						while ((nextLine = banned.readLine()) != null) {
							if (nextLine.contains("#"))
								continue;
							else {
								String[] details = nextLine.split("\\|");

								if (details.length < 4)
									continue;
								else if (!Util.isValidPlayerName(details[0]))
									continue;
								else if (plugin.dbLogger.playerInTable(details[0]))
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

									toBan.add(new BanData(pName, expires, pReason, date, by));
								}
							}
						}
					} catch (IOException | ParseException e) {
						Util.sendMessage(sender, ChatColor.RED + "Error occurred");
						BanImportCommand.importInProgress = false;
						return;
					}

					Util.sendMessage(sender, plugin.banMessages.get("scanPlayersFound").replace("[found]", Integer.toString(toBan.size())));

					if (toBan.size() == 0) {
						Util.sendMessage(sender, plugin.banMessages.get("noPlayersImport"));
						return;
					} else {

						int done = 0;
						double percent = 0;
						double newPercent;
						int totalPlayers = toBan.size();

						for (BanData p : toBan) {
							if (p.getExpires() == 0)
								plugin.dbLogger.logBan(p.getBanned(), p.getBy(), p.getReason());
							else
								plugin.dbLogger.logTempBan(p.getBanned(), p.getBy(), p.getReason(), p.getExpires());

							done++;

							newPercent = Math.round((double) (done * 100) / totalPlayers);
							if (newPercent != percent) {
								percent = newPercent;
								if (percent % 10 == 0 && (int) percent != 100)
									Util.sendMessage(sender, plugin.banMessages.get("percentagePlayersImported").replace("[percent]", Double.toString(percent)));
							}
						}

						toBan.clear();

						Util.sendMessage(sender, plugin.banMessages.get("playerImportComplete"));

						BanImportCommand.importInProgress = false;
					}
				}
			});
		} else if (type.equals("ip")) {
			Util.sendMessage(sender, plugin.banMessages.get("beginingIpImport"));

			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					BufferedReader banned;
					try {
						banned = new BufferedReader(new FileReader("banned-ips.txt"));
					} catch (FileNotFoundException e) {
						Util.sendMessage(sender, "banned-ips.txt not found");
						return;
					}

					Util.sendMessage(sender, plugin.banMessages.get("scanningDatabase"));

					String nextLine = "";

					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

					ArrayList<BanData> toBan = new ArrayList<BanData>();

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
								else if (plugin.dbLogger.ipInTable(details[0]))
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

									toBan.add(new BanData(pName, expires, pReason, date, by));
								}
							}
						}
					} catch (IOException | ParseException e) {
						Util.sendMessage(sender, ChatColor.RED + "Error occurred");
						BanImportCommand.importInProgress = false;
						return;
					}

					Util.sendMessage(sender, plugin.banMessages.get("scanPlayersFound").replace("[found]", Integer.toString(toBan.size())));

					if (toBan.size() == 0) {
						Util.sendMessage(sender, plugin.banMessages.get("noIpsImport"));
						return;
					} else {

						int done = 0;
						double percent = 0;
						double newPercent;
						int totalPlayers = toBan.size();

						for (BanData p : toBan) {
							if (p.getExpires() == 0)
								plugin.dbLogger.logIpBan(p.getBanned(), p.getBy(), p.getReason());
							else
								plugin.dbLogger.logTempIpBan(p.getBanned(), p.getBy(), p.getReason(), p.getExpires());

							done++;

							newPercent = Math.round((double) (done * 100) / totalPlayers);
							if (newPercent != percent) {
								percent = newPercent;
								if (percent % 10 == 0 && (int) percent != 100)
									Util.sendMessage(sender, plugin.banMessages.get("percentageIpsImported").replace("[percent]", Double.toString(percent)));
							}
						}

						toBan.clear();

						Util.sendMessage(sender, plugin.banMessages.get("ipImportComplete"));

						BanImportCommand.importInProgress = false;
					}
				}

			});
		}

		return true;
	}
}
