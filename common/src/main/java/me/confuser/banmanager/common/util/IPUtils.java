package me.confuser.banmanager.common.util;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressNetwork;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4AddressSeqRange;
import inet.ipaddr.ipv6.IPv6AddressSeqRange;
import me.confuser.banmanager.common.CommonPlayer;
import org.apache.commons.net.util.SubnetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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

  public static IPAddress toIPAddress(String ip) {
    try {
      return new IPAddressString(ip).toAddress().getLower();
    } catch (AddressStringException e) {
      return null;
    }
  }
}
