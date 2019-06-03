package me.confuser.banmanager.configs;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.Getter;
import me.confuser.banmanager.common.config.ConfigKeyTypes;
import me.confuser.banmanager.common.config.ConfigKeys;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static me.confuser.banmanager.BanManager.plugin;

public class GeoIpConfig {

  @Getter
  private boolean enabled = false;
  @Getter
  private DatabaseReader cityDatabase;
  @Getter
  private DatabaseReader countryDatabase;
  private List<String> countries;
  @Getter
  private String type;

  public GeoIpConfig() {
    //TODO
    //super("geoip.yml");
  }

  @Override
  public void afterLoad() {
    enabled = plugin.getConfiguration().get(ConfigKeys.GEOIP_ENABLED);

    if (!enabled) return;

    File cityFile = new File(plugin.getBootstrap().getDataDirectory().toFile(), "city.mmdb");
    String cityDownloadUrl = plugin.getConfiguration().get(ConfigKeys.GEOIP_DOWNLOAD_CITY);
    File countryFile = new File(plugin.getBootstrap().getDataDirectory().toFile(), "country.mmdb");
    String countryDownloadUrl = plugin.getConfiguration().get(ConfigKeys.GEOIP_DOWNLOAD_COUNTRY);

    long lastUpdated = plugin.getConfiguration().get(ConfigKeys.GEOIP_DOWNLOAD_LASTUPDATED);
    boolean outdated = (System.currentTimeMillis() - lastUpdated) > 2592000000L; // older than 30 days?

    if (!cityFile.exists() || outdated) {
      plugin.getLogger().info("Downloading city database");
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
      plugin.getLogger().info("Downloading country database");
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
      plugin.getLogger().info("Loading city database");
      try {
        cityDatabase = new DatabaseReader.Builder(cityFile).build();
      } catch (IOException e) {
        plugin.getLogger().severe("Failed loading city database");
        enabled = false;
        e.printStackTrace();
        return;
      }
    }

    if (countryFile.exists()) {
      plugin.getLogger().info("Loading country database");
      try {
        countryDatabase = new DatabaseReader.Builder(countryFile).build();
      } catch (IOException e) {
        plugin.getLogger().severe("Failed loading city database");
        enabled = false;
        e.printStackTrace();
        return;
      }
    }

    if (!enabled) return;

    if (outdated) {
      plugin.getConfiguration().set((ConfigKeyTypes.FunctionalKey)ConfigKeys.GEOIP_DOWNLOAD_LASTUPDATED, System.currentTimeMillis());
      plugin.getConfiguration().save();
    }

    plugin.getLogger().info("Successfully loaded GeoIP databases");

    countries = plugin.getConfiguration().get(ConfigKeys.GEOIP_COUNTRIES_LIST);
    type = plugin.getConfiguration().get(ConfigKeys.GEOIP_COUNTRIES_TYPE);

    plugin.getLogger().info("Loaded " + countries.size() + " countries on the " + type);
  }

  public boolean isCountryAllowed(CountryResponse countryResponse) {
    if (type.equals("blacklist")) {
      return !countries.contains(countryResponse.getCountry().getIsoCode());
    } else {
      return countries.contains(countryResponse.getCountry().getIsoCode());
    }
  }

  private void downloadDatabase(String downloadUrl, File location) throws IOException {
    if (location.exists()) {
      location.delete();
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
