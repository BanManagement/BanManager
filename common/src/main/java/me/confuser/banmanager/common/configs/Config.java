package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.InvalidConfigurationException;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Config {

  public YamlConfiguration conf = new YamlConfiguration();
  protected File file = null;
  protected File dataFolder;
  @Getter
  protected CommonLogger logger;

  public Config(File dataFolder, File file, CommonLogger logger) {
    this.dataFolder = dataFolder;
    this.logger = logger;

    setFile(file);
  }

  public Config(File dataFolder, String fileName, CommonLogger logger) {
    this(dataFolder, new File(dataFolder, fileName), logger);
  }

  /**
   * Must be called before using config.load() or config.save();
   *
   * @param input
   *
   * @return (Config) instance
   */
  public Config setFile(File input) {
    // handle the File
    if (input == null) {
      new InvalidConfigurationException("File cannot be null!").printStackTrace();
    }

    file = input;

    return this;
  }

  /**
   * Lazy load
   */
  public void load() {
    if (file != null) {
      try {
        onLoad(file);

        afterLoad();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      new InvalidConfigurationException("File cannot be null!").printStackTrace();
    }
  }

  private void saveResource(String resourcePath, boolean replace) {
    if (resourcePath == null || resourcePath.equals("")) {
      throw new IllegalArgumentException("ResourcePath cannot be null or empty");
    }

    resourcePath = resourcePath.replace('\\', '/');
    InputStream in = getResource(resourcePath);
    if (in == null) {
      throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + file);
    }

    File outFile = new File(dataFolder, resourcePath);
    int lastIndex = resourcePath.lastIndexOf('/');
    File outDir = new File(dataFolder, resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

    if (!outDir.exists()) {
      outDir.mkdirs();
    }

    try {
      if (!outFile.exists() || replace) {
        OutputStream out = new FileOutputStream(outFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        out.close();
        in.close();
      } else {
        logger.warning("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile
                .getName() + " already exists.");
      }
    } catch (IOException ex) {
      logger.severe("Could not save " + outFile.getName() + " to " + outFile + " " + ex.toString());
    }
  }

  private InputStream getResource(String filename) {
    if (filename == null) {
      throw new IllegalArgumentException("Filename cannot be null");
    }

    try {
      URL url = getClass().getClassLoader().getResource(filename);

      if (url == null) {
        return null;
      }

      URLConnection connection = url.openConnection();
      connection.setUseCaches(false);
      return connection.getInputStream();
    } catch (IOException ex) {
      return null;
    }
  }

  public abstract void afterLoad();

  public void setDefaults() {
    saveResource(file.getName(), false);
  }

  /**
   * Lazy save
   *
   * @throws Exception
   */
  public abstract void onSave();

  public void onLoad(File file) throws Exception {
    if (!file.exists()) {
      if (file.getParentFile() != null)
        file.getParentFile().mkdirs();

      // Set the defaults first
      setDefaults();
    }

    try {
      conf.load(file);
    } catch (InvalidConfigurationException e) {
      logger.severe("Invalid yaml file " + file.getName());
      e.printStackTrace();

      logger.severe("Attempting to load default " + file.getName());

      file.renameTo(new File(dataFolder, file.getName().replace(".yml", "") + new SimpleDateFormat
              ("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".yml.invalid"));
      setDefaults();

      onLoad(file);
    }

    // Look for defaults in the jar
    Reader defConfigStream = new InputStreamReader(getResource(file.getName()), "UTF8");
    if (defConfigStream != null) {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
      conf.setDefaults(defConfig);
      conf.options().copyDefaults(true);
    }

    save();
  }

  public void save() {
    onSave();

    try {
      conf.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
