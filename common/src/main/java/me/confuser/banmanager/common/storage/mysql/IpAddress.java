package me.confuser.banmanager.common.storage.mysql;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.ByteArrayType;
import inet.ipaddr.AddressValueException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressNetwork;
import lombok.SneakyThrows;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.util.IPUtils;

public class IpAddress extends ByteArrayType {

  private static final IpAddress singleTon = new IpAddress();

  protected IpAddress() {
    super(SqlType.BYTE_ARRAY, new Class<?>[0]);
  }

  @Override
  public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
    IPAddress value = (IPAddress) javaObject;

    if (value == null) return null;

    return value.getBytes();
  }

  @SneakyThrows
  @Override
  public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
    if (sqlArg == null) return null;

    byte[] value = (byte[]) sqlArg;

    try {
      return new IPAddressNetwork.IPAddressGenerator().from(value).getLower();
    } catch (AddressValueException e) {
      return IPUtils.toIPAddress("127.0.0.1");
    }

  }

  /* This cannot be lombok. */
  public static IpAddress getSingleton() {
    return singleTon;
  }
}
