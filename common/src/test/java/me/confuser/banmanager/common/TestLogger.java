package me.confuser.banmanager.common;

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
}

