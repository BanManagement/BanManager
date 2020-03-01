package me.confuser.banmanager.bungee;

import me.confuser.banmanager.common.CommonLogger;

import java.util.logging.Logger;

public class PluginLogger implements CommonLogger {

  private final Logger logger;

  public PluginLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void info(String msg) {
    logger.info(msg);
  }

  @Override
  public void warning(String msg) {
    logger.warning(msg);
  }

  @Override
  public void severe(String msg) {
    logger.severe(msg);
  }
}
