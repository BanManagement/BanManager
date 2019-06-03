package me.confuser.banmanager.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtils {

  public static String getIP(InetAddress ip) {
    return ip.getHostAddress().replace("/", "");
  }

  public static long toLong(String ip) {
    String[] addressArray = ip.split("\\.");
    long result = 0;

    for (int i = 0; i < addressArray.length; i++) {
      int power = 3 - i;

      result += ((Integer.parseInt(addressArray[i]) % 256 * Math.pow(256, power)));
    }

    return result;
  }

  public static long toLong(InetAddress ip) {
    return toLong(getIP(ip));
  }

  public static String toString(long ip) {
    return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
  }

  public static InetAddress toInetAddress(long ip) throws UnknownHostException {
    return InetAddress.getByName(toString(ip));
  }

  public static long[] getRangeFromCidrNotation(String ipStr) {
    SubnetUtils.SubnetInfo info;
    try {
      info = new SubnetUtils(ipStr).getInfo();
    } catch (IllegalArgumentException e) {
      return null;
    }

    return new long[] { IPUtils.toLong(info.getLowAddress()), IPUtils.toLong(info.getHighAddress()) };
  }

  public static long[] getRangeFromWildcard(String ipStr) {
    String[] ocelots = ipStr.split("\\.");

    if (ocelots.length != 4) return null;

    String[] fromIp = new String[4];
    String[] toIp = new String[4];

    for (int i = 0; i < ocelots.length; i++) {
      if (ocelots[i].equals("*")) {
        fromIp[i] = "0";
      } else {
        fromIp[i] = ocelots[i];
      }
    }

    for (int i = 0; i < ocelots.length; i++) {
      if (ocelots[i].equals("*")) {
        toIp[i] = "255";
      } else {
        toIp[i] = ocelots[i];
      }
    }

    long fromIpAddress = IPUtils.toLong(StringUtils.join(fromIp, "."));
    long toIpAddress = IPUtils.toLong(StringUtils.join(toIp, "."));

    return new long[] { fromIpAddress, toIpAddress };
  }

}
