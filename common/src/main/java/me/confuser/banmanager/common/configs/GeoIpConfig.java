package me.confuser.banmanager.common.configs;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

public class GeoIpConfig extends Config {

  @Getter
  private boolean enabled = false;
  @Getter
  private DatabaseReader cityDatabase;
  @Getter
  private DatabaseReader countryDatabase;
  private HashSet<String> countries;
  @Getter
  private String type;

  public GeoIpConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "geoip.yml", logger);
  }

  @Override
  public void afterLoad() {
    enabled = conf.getBoolean("enabled", false);

    if (!enabled) return;

    File cityFile = new File(dataFolder, "city.mmdb");
    String cityDownloadUrl = conf.getString("download.city");
    File countryFile = new File(dataFolder, "country.mmdb");
    String countryDownloadUrl = conf.getString("download.country");

    long lastUpdated = conf.getLong("download.lastUpdated");
    boolean outdated = (System.currentTimeMillis() - lastUpdated) > 2592000000L; // older than 30 days?

    if (!cityFile.exists() || outdated) {
      logger.info("Downloading city database");
      try {
        downloadDatabase(cityDownloadUrl, cityFile);
      } catch (IOException e) {
        enabled = false;
        logger.severe("Unable to download city database");
        e.printStackTrace();
        return;
      }
    }

    if (!countryFile.exists() || outdated) {
      logger.info("Downloading country database");
      try {
        downloadDatabase(countryDownloadUrl, countryFile);
      } catch (IOException e) {
        enabled = false;
        logger.severe("Unable to download country database");
        e.printStackTrace();
        return;
      }
    }

    if (cityFile.exists()) {
      logger.info("Loading city database");
      try {
        cityDatabase = new DatabaseReader.Builder(cityFile).build();
      } catch (IOException e) {
        logger.severe("Failed loading city database");
        enabled = false;
        e.printStackTrace();
        return;
      }
    }

    if (countryFile.exists()) {
      logger.info("Loading country database");
      try {
        countryDatabase = new DatabaseReader.Builder(countryFile).build();
      } catch (IOException e) {
        logger.severe("Failed loading country database");
        enabled = false;
        e.printStackTrace();
        return;
      }
    }

    if (!enabled) return;

    if (outdated) {
      conf.set("download.lastUpdated", System.currentTimeMillis());
      save();
    }

    logger.info("Successfully loaded GeoIP databases");

    countries = new HashSet<>(conf.getStringList("countries.list"));
    type = conf.getString("countries.type");

    logger.info("Loaded " + countries.size() + " countries on the " + type);
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
