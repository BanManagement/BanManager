package me.confuserr.banmanager;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Util {

	private static BanManager plugin = BanManager.getPlugin();
	private static final Random generator = new Random();

	public static String viewReason(String reason) {
		return reason.replace("&quot;", "\"").replace("&#039;", "'").replace("\\n", "\n");
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

	@SuppressWarnings("deprecation")
	public static void asyncQuery(final String query) {
		if (plugin.getConfig().getBoolean("useSyncChat")) {
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					plugin.localConn.query(query);
				}

			});
		} else {
			try {
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					public void run() {
						plugin.localConn.query(query);
					}

				});
			} catch (NoSuchMethodError e) {

			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void asyncQuery(final String query, final Database extConn) {
		if (plugin.getConfig().getBoolean("useSyncChat")) {
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					extConn.query(query);
				}

			});
		} else {
			try {
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					public void run() {
						extConn.query(query);
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
			int i;

			try {
				i = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				return false;
			}

			if ((i < 0) || (i > 255))
				return false;
		}

		return true;
	}

	public static String getIP(InetAddress ip) {
		return ip.getHostAddress().replace("/", "");
	}

	public static String getIP(String ip) {
		ip = ip.replace("/", "");

		String[] withoutPort = ip.split(":");

		if (withoutPort.length == 2)
			return withoutPort[0];
		else
			return ip;
	}

	public static long getTimeStamp(String time) {
		// TODO Auto-generated method stub
		long timeReturn;
		try {
			timeReturn = parseDateDiff(time, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			timeReturn = 0;
		}
		return timeReturn;
	}
	
	public static int generatePin() {
		return 100000 + generator.nextInt(900000);
	}

	public static boolean isValidPlayerName(String name) {
		return name.matches("^[a-zA-Z0-9_]*$");
	}
	
	public static boolean ipBukkitBanned(String ip) {
		Set<String> bans = plugin.getServer().getIPBans();
		if (bans.contains(ip))
			return true;
		return false;
	}

	// Copyright essentials, all credits to them, this is here to remove
	// dependency on it, I did not create these functions!
	public static long parseDateDiff(String time, boolean future) throws Exception {
		Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
		Matcher m = timePattern.matcher(time);
		int years = 0;
		int months = 0;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		boolean found = false;
		while (m.find()) {
			if (m.group() == null || m.group().isEmpty()) {
				continue;
			}
			for (int i = 0; i < m.groupCount(); i++) {
				if (m.group(i) != null && !m.group(i).isEmpty()) {
					found = true;
					break;
				}
			}
			if (found) {
				if (m.group(1) != null && !m.group(1).isEmpty())
					years = Integer.parseInt(m.group(1));
				if (m.group(2) != null && !m.group(2).isEmpty())
					months = Integer.parseInt(m.group(2));
				if (m.group(3) != null && !m.group(3).isEmpty())
					weeks = Integer.parseInt(m.group(3));
				if (m.group(4) != null && !m.group(4).isEmpty())
					days = Integer.parseInt(m.group(4));
				if (m.group(5) != null && !m.group(5).isEmpty())
					hours = Integer.parseInt(m.group(5));
				if (m.group(6) != null && !m.group(6).isEmpty())
					minutes = Integer.parseInt(m.group(6));
				if (m.group(7) != null && !m.group(7).isEmpty())
					seconds = Integer.parseInt(m.group(7));
				break;
			}
		}
		if (!found)
			throw new Exception("Illegal Date");
		Calendar c = new GregorianCalendar();
		if (years > 0)
			c.add(Calendar.YEAR, years * (future ? 1 : -1));
		if (months > 0)
			c.add(Calendar.MONTH, months * (future ? 1 : -1));
		if (weeks > 0)
			c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
		if (days > 0)
			c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
		if (hours > 0)
			c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
		if (minutes > 0)
			c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
		if (seconds > 0)
			c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
		return c.getTimeInMillis();
	}

	public static String formatDateDiff(Calendar fromDate, Calendar toDate) {
		boolean future = false;
		if (toDate.equals(fromDate)) {
			return plugin.getMessage("timeNow");
		}
		if (toDate.after(fromDate)) {
			future = true;
		}

		StringBuilder sb = new StringBuilder();
		int[] types = new int[] { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
		String[] names = new String[] { plugin.getMessage("timeYear"), plugin.getMessage("timeYears"), plugin.getMessage("timeMonth"), plugin.getMessage("timeMonths"), plugin.getMessage("timeDay"), plugin.getMessage("timeDays"), plugin.getMessage("timeHour"), plugin.getMessage("timeHours"), plugin.getMessage("timeMinute"), plugin.getMessage("timeMinutes"), plugin.getMessage("timeSecond"), plugin.getMessage("timeSeconds") };
		for (int i = 0; i < types.length; i++) {
			int diff = dateDiff(types[i], fromDate, toDate, future);
			if (diff > 0) {
				sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
			}
		}
		if (sb.length() == 0) {
			return "now";
		}
		return sb.toString().trim();
	}

	public static String formatDateDiff(long date) {
		Calendar now = new GregorianCalendar();
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(date);
		return formatDateDiff(now, c);
	}

	private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
		int diff = 0;
		long savedDate = fromDate.getTimeInMillis();
		while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
			savedDate = fromDate.getTimeInMillis();
			fromDate.add(type, future ? 1 : -1);
			diff++;
		}
		diff--;
		fromDate.setTimeInMillis(savedDate);
		return diff;
	}
}
