package me.confuser.banmanager.common.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.Fetcher;
import me.confuser.banmanager.common.gson.Gson;
import me.confuser.banmanager.common.gson.JsonObject;

/**
 * Based on UUIDFetcher by evilmidget38
 */
public class UUIDUtils {
  private static HttpRequest buildGet(String urlStr) {
    return HttpRequest.newBuilder()
        .uri(URI.create(urlStr))
        .timeout(Duration.ofSeconds(15))
        .header("Content-Type", "application/json")
        .header("User-Agent", "BanManager")
        .GET()
        .build();
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
    Fetcher fetcher = plugin.getConfig().getUuidFetcher().nameToId();
    String url = fetcher.url().replace("[name]", name);

    HttpResponse<String> response = sendBlocking(plugin.getHttpClient(), buildGet(url));
    int status = response.statusCode();

    plugin.getLogger().info(url + " " + status);

    if (status != 200) throw new Exception("Error retrieving UUID from " + url);

    JsonObject data = new Gson().fromJson(response.body(), JsonObject.class);
    return new UUIDProfile(name, UUIDUtils.getUUID(data.get(fetcher.key()).getAsString()));
  }

  public static String getCurrentName(BanManagerPlugin plugin, UUID uuid) throws Exception {
    plugin.getLogger().info("Requesting name for " + uuid.toString());
    Fetcher fetcher = plugin.getConfig().getUuidFetcher().idToName();
    String url = fetcher.url().replace("[uuid]", uuid.toString());

    HttpResponse<String> response = sendBlocking(plugin.getHttpClient(), buildGet(url));
    int status = response.statusCode();

    plugin.getLogger().info(url + " " + status);

    if (status != 200) throw new Exception("Error retrieving name from " + url);

    JsonObject data = new Gson().fromJson(response.body(), JsonObject.class);
    return data.get(fetcher.key()).getAsString();
  }

  public static UUID createOfflineUUID(String name) {
    return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
  }

  private static HttpResponse<String> sendBlocking(HttpClient client, HttpRequest request) throws Exception {
    return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }
}
