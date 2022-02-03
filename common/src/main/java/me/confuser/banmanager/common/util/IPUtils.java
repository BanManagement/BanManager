package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.ipaddr.AddressStringException;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ipaddr.IPAddressNetwork;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import me.confuser.banmanager.common.ipaddr.ipv4.IPv4AddressSeqRange;
import me.confuser.banmanager.common.ipaddr.ipv6.IPv6AddressSeqRange;

import java.net.InetAddress;

public class IPUtils {

  public static byte[] toBytes(String ip) {
    return toIPAddress(ip).getBytes();
  }

  public static String toString(byte[] ip) {
    return new IPAddressNetwork.IPAddressGenerator().from(ip).getLower().toString();
  }

  public static boolean isInRange(IPAddress fromIp, IPAddress toIp, IPAddress ip) {
    if (!fromIp.getIPVersion().equals(toIp.getIPVersion())) return false;
    if (!fromIp.getIPVersion().equals(ip.getIPVersion())) return false;

    if (ip.isIPv4()) {
      return new IPv4AddressSeqRange(fromIp.toIPv4(), toIp.toIPv4()).contains(ip);
    } else {
      return new IPv6AddressSeqRange(fromIp.toIPv6(), toIp.toIPv6()).contains(ip);
    }
  }

  public static IPAddress toIPAddress(InetAddress address) {
    return new IPAddressNetwork.IPAddressGenerator().from(address).getLower();
  }

  public static IPAddress toIPAddress(byte[] bytes) {
    return new IPAddressNetwork.IPAddressGenerator().from(bytes).getLower();
  }

  public static boolean isValid(String ip) {
    return new IPAddressString(ip).isValid();
  }

  public static IPAddress toIPAddress(String ip) {
    try {
      return new IPAddressString(ip).toAddress().getLower();
    } catch (AddressStringException e) {
      return null;
    }
  }
}
