package me.confuser.banmanager.fabric;

import org.apache.logging.log4j.Logger;

import me.confuser.banmanager.common.CommonLogger;

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
    logger.warn(msg);
  }

  @Override
  public void severe(String msg) {
    logger.error(msg);
  }
}
