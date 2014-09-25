package me.confuser.banmanager.util;

import java.net.InetAddress;

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
}
