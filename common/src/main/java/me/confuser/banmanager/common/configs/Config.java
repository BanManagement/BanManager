package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.InvalidConfigurationException;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class Config {

  public YamlConfiguration conf = new YamlConfiguration();
  protected File file = null;
  protected File dataFolder;
  @Getter
  protected CommonLogger logger;
  @Getter
  private boolean safeToSave = false;

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
   *
   * @return true if the config was loaded successfully, false otherwise
   */
  public boolean load() {
    if (file == null) {
      new InvalidConfigurationException("File cannot be null!").printStackTrace();
      return false;
    }

    try {
      if (!onLoad(file)) {
        safeToSave = false;
        return false;
      }

      // Allow configs to persist migrations/defaults during afterLoad().
      safeToSave = true;
      afterLoad();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      safeToSave = false;
      return false;
    }
  }

  public abstract void afterLoad();

  public abstract void onSave();

  /**
   * Load configuration from file
   *
   * @param file the file to load
   * @return true if the config was loaded successfully, false otherwise
   * @throws Exception if the file does not exist
   */
  public boolean onLoad(File file) throws Exception {
    if (!file.exists()) {
      throw new Exception("File " + file.getName() + " does not exist");
    }

    try {
      conf.load(file);
      return true;
    } catch (InvalidConfigurationException e) {
      logger.severe("Invalid yaml file " + file.getName());
      return false;
    }
  }

  public void save() {
    if (!safeToSave) {
      logger.warning("Skipping save for " + file.getName() + " - config was not loaded successfully");
      return;
    }

    onSave();

    try {
      conf.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
