package me.confuser.banmanager.common.util;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.UUID;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.Fetcher;
import me.confuser.banmanager.common.gson.Gson;
import me.confuser.banmanager.common.gson.JsonObject;

/**
 * Based on UUIDFetcher by evilmidget38
 */
public class UUIDUtils {
  private static HttpURLConnection createConnection(String urlStr, String method) throws Exception {
    URL url = new URL(urlStr);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setUseCaches(false);
    connection.setDoInput(true);

    if (method.equals("POST")) connection.setDoOutput(true);
    return connection;
  }

  private static UUID getUUID(String id) {
    return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id
            .substring(16, 20) + "-" + id.substring(20, 32));
  }

  public static byte[] toBytes(UUID uuid) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
    byteBuffer.putLong(uuid.getMostSignificantBits());
    byteBuffer.putLong(uuid.getLeastSignificantBits());
    return byteBuffer.array();
  }

  public static UUID fromBytes(byte[] array) {
    if (array.length != 16) {
      throw new IllegalArgumentException("Illegal byte array length: " + array.length);
    }
    ByteBuffer byteBuffer = ByteBuffer.wrap(array);
    long mostSignificant = byteBuffer.getLong();
    long leastSignificant = byteBuffer.getLong();
    return new UUID(mostSignificant, leastSignificant);
  }

  public static UUIDProfile getUUIDOf(BanManagerPlugin plugin, String name) throws Exception {
    if (!plugin.getConfig().isOnlineMode()) {
      plugin.getLogger().info("Generating offline UUID for " + name);

      return new UUIDProfile(name, createOfflineUUID(name));
    }

    plugin.getLogger().info("Requesting UUID for " + name);
    Fetcher fetcher = plugin.getConfig().getUuidFetcher().getNameToId();
    String url = fetcher.getUrl().replace("[name]", name);

    HttpURLConnection connection = createConnection(url, "GET");

    int status = connection.getResponseCode();

    plugin.getLogger().info(url + " " + status);

    if (status != 200) throw new Exception("Error retrieving UUID from " + url);

    JsonObject data = new Gson().fromJson(new InputStreamReader(connection.getInputStream()),
        JsonObject.class);

    return new UUIDProfile(name, UUIDUtils.getUUID(data.get(fetcher.getKey()).getAsString()));
  }

  public static String getCurrentName(BanManagerPlugin plugin, UUID uuid) throws Exception {
    plugin.getLogger().info("Requesting name for " + uuid.toString());
    Fetcher fetcher = plugin.getConfig().getUuidFetcher().getIdToName();
    String url = fetcher.getUrl().replace("[uuid]", uuid.toString());

    HttpURLConnection connection = createConnection(url, "GET");

    int status = connection.getResponseCode();

    plugin.getLogger().info(url + " " + status);

    if (status != 200) throw new Exception("Error retrieving name from " + url);

    JsonObject data = new Gson().fromJson(new InputStreamReader(connection.getInputStream()),
        JsonObject.class);

    return data.get(fetcher.getKey()).getAsString();
  }

  public static UUID createOfflineUUID(String name) {
    try {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }
}
