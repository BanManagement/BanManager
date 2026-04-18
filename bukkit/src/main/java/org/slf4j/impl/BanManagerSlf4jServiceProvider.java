package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * SLF4J 2.x service provider that wires {@link BanManagerLoggerFactory} into
 * the SLF4J API. Discovered via {@code META-INF/services/org.slf4j.spi.SLF4JServiceProvider}
 * (also relocated alongside the rest of {@code org.slf4j} so the shaded
 * HikariCP / ORMLite pick it up automatically).
 */
public class BanManagerSlf4jServiceProvider implements SLF4JServiceProvider {

  // Track the SLF4J API version this provider was compiled against. Must be
  // updated when bumping the slf4j-api dependency to keep the version-mismatch
  // warning accurate.
  public static final String REQUESTED_API_VERSION = "2.0.99";

  private ILoggerFactory loggerFactory;
  private IMarkerFactory markerFactory;
  private MDCAdapter mdcAdapter;

  @Override
  public void initialize() {
    loggerFactory = new BanManagerLoggerFactory();
    markerFactory = new BasicMarkerFactory();
    mdcAdapter = new BasicMDCAdapter();
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }

  @Override
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }

  @Override
  public MDCAdapter getMDCAdapter() {
    return mdcAdapter;
  }

  @Override
  public String getRequestedApiVersion() {
    return REQUESTED_API_VERSION;
  }
}
