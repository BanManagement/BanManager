package me.confuser.banmanager.util;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.j256.ormlite.dao.GenericRawResults;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.Message;

public class DateUtils {
	private static BanManager plugin = BanManager.getPlugin();
	private static long timeDiff = 0;
	
	public static String formatDifference(long time) {
		long day = TimeUnit.SECONDS.toDays(time);
		long hours = TimeUnit.SECONDS.toHours(time) - (day * 24);
		long minutes = TimeUnit.SECONDS.toMinutes(time) - (TimeUnit.SECONDS.toHours(time) * 60);
		long seconds = TimeUnit.SECONDS.toSeconds(time) - (TimeUnit.SECONDS.toMinutes(time) * 60);
		
		StringBuilder sb = new StringBuilder();
		
		if (day > 0) {
			sb.append(day)
			  .append(" ")
			  .append(Message.get(day == 1 ? "timeDay" : "timeDays").toString())
			  .append(" ");
		}
		
		if (hours > 0) {
			sb.append(hours)
			  .append(" ")
			  .append(Message.get(day == 1 ? "timeHour" : "timeHours").toString())
			  .append(" ");
		}
		
		if (minutes > 0) {
			sb.append(minutes)
			  .append(" ")
			  .append(Message.get(day == 1 ? "timeMinute" : "timeMinutes").toString())
			  .append(" ");
		}
		
		if (seconds > 0) {
			sb.append(seconds)
			  .append(" ")
			  .append(Message.get(day == 1 ? "timeSeconds" : "timeSeconds").toString());
		}

		return sb.toString();
	}
	
	public static String getDifferenceFormat(long timestamp) {
		return formatDifference(timestamp - (System.currentTimeMillis() / 1000));
	}
	
	// Copyright essentials, all credits to them for this.
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

		if (years > 20)
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
		return c.getTimeInMillis() / 1000;
	}
	
	public static long getTimeDiff() {
		return timeDiff;
	}
	
	public static long findTimeDiff() throws SQLException {
		String query = "SELECT UNIX_TIMESTAMP() - ? as mysqlTime";
		
		GenericRawResults<String[]> results = plugin.getPlayerStorage().queryRaw(query, String.valueOf(System.currentTimeMillis() / 1000));
		
		timeDiff = Long.parseLong(results.getFirstResult()[0]);
		
		return timeDiff;
	}
}
