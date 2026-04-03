package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BanManagerLoggerFactory implements ILoggerFactory {

  private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();

  @Override
  public Logger getLogger(String name) {
    Logger existing = loggerMap.get(name);
    if (existing != null) {
      return existing;
    }
    Logger newLogger = new BanManagerSlf4jLogger(name);
    Logger oldLogger = loggerMap.putIfAbsent(name, newLogger);
    return oldLogger == null ? newLogger : oldLogger;
  }
}
