package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * SLF4J 1.7.x binding entry point. Shadow relocates this alongside the rest of
 * org.slf4j so the shaded HikariCP / ORMLite pick it up automatically.
 *
 * <p>If ever upgrading to SLF4J 2.x, this class must be replaced with a
 * {@code SLF4JServiceProvider} registered via {@code META-INF/services}.</p>
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

  private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

  // Must NOT be final so the compiler doesn't inline it.
  public static String REQUESTED_API_VERSION = "1.6.99";

  private final ILoggerFactory loggerFactory;

  private StaticLoggerBinder() {
    loggerFactory = new BanManagerLoggerFactory();
  }

  public static StaticLoggerBinder getSingleton() {
    return SINGLETON;
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }

  @Override
  public String getLoggerFactoryClassStr() {
    return BanManagerLoggerFactory.class.getName();
  }
}
