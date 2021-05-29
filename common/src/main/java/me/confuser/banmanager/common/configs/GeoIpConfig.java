package me.confuser.banmanager.common.configs;

import com.maxmind.db.GeoIp2Provider;
import com.maxmind.db.cache.CHMCache;
import com.maxmind.db.Reader.FileMode;
import com.maxmind.db.Reader;
import com.maxmind.db.model.CountryResponse;
import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

public class GeoIpConfig extends Config {

  @Getter
  private boolean enabled = false;
  @Getter
  private GeoIp2Provider cityDatabase;
  @Getter
  private GeoIp2Provider countryDatabase;
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

    if (!conf.getString("download.country").contains("licenseKey") || !conf.getString("download.city").contains("licenseKey")) {
      // Migrate to GeoIP2
      conf.set("download.city", "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key=[licenseKey]&suffix=tar.gz");
      conf.set("download.country", "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key=[licenseKey]&suffix=tar.gz");

      save();
    }

    String licenseKey = conf.getString("download.licenseKey");

    if (licenseKey == null || licenseKey.isEmpty()) {
      logger.severe("Unable to enable geoip features due to missing licenseKey");
      return;
    }

    String cityDownloadUrl = conf.getString("download.city").replace("[licenseKey]", licenseKey);
    String countryDownloadUrl = conf.getString("download.country").replace("[licenseKey]", licenseKey);
    File cityFile = new File(dataFolder, "city.mmdb");
    File countryFile = new File(dataFolder, "country.mmdb");

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
        cityDatabase =  new Reader(cityFile, FileMode.MEMORY, new CHMCache());
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
        countryDatabase = new Reader(cityFile, FileMode.MEMORY, new CHMCache());
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
    // blacklist option deprecated
    if (type.equals("blacklist") || type.equals("deny")) {
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
    TarArchiveInputStream inputStream = new TarArchiveInputStream(input);

    FileOutputStream outputStream = new FileOutputStream(location);

    ArchiveEntry entry = null;
    while ((entry = inputStream.getNextEntry()) != null) {
      if (entry.isDirectory()) continue;
      if (!entry.getName().endsWith(".mmdb")) continue;

      IOUtils.copy(inputStream, outputStream);

      break;
    }

    outputStream.close();
    inputStream.close();
    input.close();
  }
}
