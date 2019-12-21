package me.confuser.banmanager.common.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import me.confuser.banmanager.common.BanManagerPlugin;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 * Based on UUIDFetcher by evilmidget38
 */
public class UUIDUtils implements Callable<Map<String, UUID>> {

  private static final double PROFILES_PER_REQUEST = 100;
  private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
  private final List<String> names;
  private final boolean rateLimiting;
  private BanManagerPlugin plugin;

  public UUIDUtils(BanManagerPlugin plugin, List<String> names, boolean rateLimiting) {
    this.plugin = plugin;
    this.names = ImmutableList.copyOf(names);
    this.rateLimiting = rateLimiting;
  }

  public UUIDUtils(BanManagerPlugin plugin, Set<String> names, boolean rateLimiting) {
    this.plugin = plugin;
    this.names = ImmutableList.copyOf(names);
    this.rateLimiting = rateLimiting;
  }

  public UUIDUtils(BanManagerPlugin plugin, List<String> names) {
    this(plugin, names, true);
  }

  private static void writeBody(HttpURLConnection connection, String body) throws Exception {
    OutputStream stream = connection.getOutputStream();
    stream.write(body.getBytes());
    stream.flush();
    stream.close();
  }

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
    Map<String, UUID> players = new UUIDUtils(plugin, Collections.singletonList(name)).call();

    if (players.isEmpty()) {
      return null;
    }

    Entry<String, UUID> player = players.entrySet().iterator().next();

    return new UUIDProfile(player.getKey(), player.getValue());
  }

  public static String getCurrentName(BanManagerPlugin plugin, UUID uuid) throws Exception {
    plugin.getLogger().info("Requesting name for " + uuid.toString());
    String url = "https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names";

    HttpURLConnection connection = createConnection(url, "GET");

    int status = connection.getResponseCode();

    if (status != 200) throw new Exception("Error retrieving name from " + url);

    JsonResponseNames array = new Gson().fromJson(new InputStreamReader(connection.getInputStream()),
            JsonResponseNames.class);

    if (array.content.size() == 0) return null;

    JsonResponseNameDetail jsonProfile = array.content.get(array.content.size() - 1);

    return jsonProfile.name;
  }

  public static UUID createOfflineUUID(String name) {
    try {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }

  public Map<String, UUID> call() throws Exception {

    Map<String, UUID> uuidMap = new HashMap<>();
    if (!plugin.getConfig().isOnlineMode()) {
      plugin.getLogger().info("Generating offline UUIDs for " + String.join(",", names));

      for (String s : names) {
        uuidMap.put(s, createOfflineUUID(s));
      }

      return uuidMap;
    }

    plugin.getLogger().info("Requesting UUIDs for " + String.join(",", names));

    int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
    for (int i = 0; i < requests; i++) {
      HttpURLConnection connection = createConnection(PROFILE_URL, "POST");
      String body = new Gson().toJson(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
      writeBody(connection, body);

      JsonResponseProfileDetail[] array = new Gson().fromJson(new InputStreamReader(connection.getInputStream()),
              JsonResponseProfileDetail[].class);

      for (JsonResponseProfileDetail profile : array) {
        UUID uuid = UUIDUtils.getUUID(profile.id);
        uuidMap.put(profile.name, uuid);
      }

      if (rateLimiting && i != requests - 1) {
        // Try to avoid rate limit
        Thread.sleep(10000L);
      }
    }
    return uuidMap;
  }

  public class JsonResponseNames {

    private List<JsonResponseNameDetail> content;
  }

  public class JsonResponseNameDetail {

    private String name;
    private long changedToAt;
  }

  public class JsonResponseProfileDetail {

    private String id;
    private String name;
  }
}
