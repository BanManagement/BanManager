package me.confuserr.banmanager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Util {

	private static BanManager plugin = BanManager.getPlugin();

	public static String viewReason(String reason) {
		return reason.replace("&quot;", "\"").replace("&#039;", "'");
	}

	public static String parseReason(String[] args) {
		String reason = Util.implodeArray(args, " ").replace("\"", "&quot;").replace("'", "&#039;");
		return reason;
	}

	public static String implodeArray(String[] inputArray, String glueString) {
		/** Output variable */
		String output = "";

		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);

			for (int i = 1; i < inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}

			output = sb.toString();
		}

		return output;
	}

	public static String getReason(String[] args, int start) {
		// Basically ignore the first item in the array
		String[] newArgs = new String[args.length - start];
		System.arraycopy(args, start, newArgs, 0, args.length - start);
		return Util.parseReason(newArgs);
	}

	public static String colorize(String string) {
		return string.replaceAll("(?i)&([a-k0-9])", "\u00A7$1");
	}

	public static void sendMessage(CommandSender sender, String message) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			String[] s = message.split("\\\\n");
			for (String m : s) {
				player.sendMessage(m);
			}
		} else {
			String[] s = message.split("\\\\n");
			for (String m : s) {
				sender.sendMessage(m);
			}
		}
	}

	public static void sendMessageWithPerm(String message, String perm) {
		for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
			if (onlinePlayer.hasPermission(perm))
				onlinePlayer.sendMessage(message);
		}
	}

	public static void asyncQuery(final String query) {
		if (plugin.getConfig().getBoolean("useSyncChat")) {
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				@Override
				public void run() {
					plugin.localConn.query(query);
				}

			});
		} else {
			try {
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						plugin.localConn.query(query);
					}

				});
			} catch (NoSuchMethodError e) {

			}
		}
	}

	public final static boolean ValidateIPAddress(String ipAddress) {
		String[] parts = ipAddress.split("\\.");

		if (parts.length != 4)
			return false;

		for (String s : parts) {
			int i = Integer.parseInt(s);

			if ((i < 0) || (i > 255))
				return false;
		}

		return true;
	}
}
