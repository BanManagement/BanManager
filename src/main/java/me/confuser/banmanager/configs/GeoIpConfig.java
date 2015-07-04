package me.confuser.banmanager.configs;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.configs.Config;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

public class GeoIpConfig extends Config<BanManager> {

  @Getter
  private boolean enabled = false;
  @Getter
  private DatabaseReader cityDatabase;
  @Getter
  private DatabaseReader countryDatabase;
  private HashSet<String> countries;
  @Getter
  private String type;

  public GeoIpConfig() {
    super("geoip.yml");
  }

  @Override
  public void afterLoad() {
    enabled = conf.getBoolean("enabled", false);

    if (!enabled) return;

    File cityFile = new File(plugin.getDataFolder(), "city.mmdb");
    String cityDownloadUrl = conf.getString("download.city");
    File countryFile = new File(plugin.getDataFolder(), "country.mmdb");
    String countryDownloadUrl = conf.getString("download.country");

    long lastUpdated = conf.getLong("download.lastUpdated");
    boolean outdated = (System.currentTimeMillis() - lastUpdated) > 2592000000L; // older than 30 days?

    if (!cityFile.exists() || outdated) {
      try {
        downloadDatabase(cityDownloadUrl, cityFile);
      } catch (IOException e) {
        enabled = false;
        plugin.getLogger().severe("Unable to download city database");
        e.printStackTrace();
        return;
      }
    }

    if (!countryFile.exists() || outdated) {
      try {
        downloadDatabase(countryDownloadUrl, countryFile);
      } catch (IOException e) {
        enabled = false;
        plugin.getLogger().severe("Unable to download country database");
        e.printStackTrace();
        return;
      }
    }

    if (cityFile.exists()) {
      try {
        cityDatabase = new DatabaseReader.Builder(cityFile).build();
      } catch (IOException e) {
        enabled = false;
        e.printStackTrace();
        return;
      }
    }

    if (countryFile.exists()) {
      try {
        countryDatabase = new DatabaseReader.Builder(countryFile).build();
      } catch (IOException e) {
        enabled = false;
        e.printStackTrace();
        return;
      }
    }

    if (!enabled) return;

    conf.set("download.lastUpdated", System.currentTimeMillis());

    plugin.getLogger().info("Successfully loaded GeoIP databases");

    countries = new HashSet<>(conf.getStringList("countries.list"));
    type = conf.getString("countries.type");

    plugin.getLogger().info("Loaded " + countries.size() + " countries on the " + type);
  }

  public boolean isCountryAllowed(CountryResponse countryResponse) {
    if (type.equals("blacklist")) {
      return !countries.contains(countryResponse.getCountry().getIsoCode());
    } else {
      return countries.contains(countryResponse.getCountry().getIsoCode());
    }
  }

  @Override
  public void onSave() {
  }

  private void downloadDatabase(String downloadUrl, File location) throws IOException {
    if (location.exists()) {
      file.delete();
    }

    URL url = new URL(downloadUrl);
    URLConnection con = url.openConnection();

    con.setConnectTimeout(6000);
    con.connect();

    InputStream input = new GZIPInputStream(con.getInputStream());

    OutputStream output = new FileOutputStream(location);

    byte[] buffer = new byte[1024];

    int length;
    while ((length = input.read(buffer)) > 0) {
      output.write(buffer, 0, length);
    }

    output.close();
    input.close();
  }
}
