package me.confuser.banmanager.common;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestLogger implements CommonLogger {

  private final Logger logger;

  public TestLogger() {
    this.logger = java.util.logging.Logger.getLogger("tests");
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

  @Override
  public void severe(String msg, Throwable t) {
    logger.log(Level.SEVERE, msg, t);
  }

  @Override
  public void warning(String msg, Throwable t) {
    logger.log(Level.WARNING, msg, t);
  }
}

