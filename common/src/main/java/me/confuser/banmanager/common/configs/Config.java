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

  public abstract void afterLoad();

  public abstract void onSave();

  public void onLoad(File file) throws Exception {
    if (!file.exists()) {
      throw new Exception("File " + file.getName() + " does not exist");
    }

    try {
      conf.load(file);
    } catch (InvalidConfigurationException e) {
      logger.severe("Invalid yaml file " + file.getName());

      return;
    }
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
