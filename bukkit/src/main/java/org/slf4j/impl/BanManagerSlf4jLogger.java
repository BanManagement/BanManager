package org.slf4j.impl;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonLogger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

/**
 * SLF4J 2.x logger that delegates to BanManager's {@link CommonLogger}.
 *
 * <p>Level filtering: when debug mode is off, TRACE / DEBUG / INFO are
 * suppressed. This replaces the old {@code disableDatabaseLogging()} system
 * property which has no effect when ORMLite uses SLF4J instead of LocalLog.</p>
 */
public class BanManagerSlf4jLogger extends LegacyAbstractLogger {

  private static final long serialVersionUID = 1L;

  BanManagerSlf4jLogger(String name) {
    this.name = name;
  }

  private CommonLogger logger() {
    BanManagerPlugin plugin = BanManagerPlugin.getInstance();
    return plugin != null ? plugin.getLogger() : null;
  }

  private boolean isDebugMode() {
    BanManagerPlugin plugin = BanManagerPlugin.getInstance();
    return plugin != null && plugin.getConfig() != null && plugin.getConfig().isDebugEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return isDebugMode();
  }

  @Override
  public boolean isDebugEnabled() {
    return isDebugMode();
  }

  @Override
  public boolean isInfoEnabled() {
    return isDebugMode();
  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  protected String getFullyQualifiedCallerName() {
    return null;
  }

  @Override
  protected void handleNormalizedLoggingCall(Level level,
                                             Marker marker,
                                             String messagePattern,
                                             Object[] arguments,
                                             Throwable throwable) {
    CommonLogger l = logger();
    if (l == null) return;

    String formatted = (arguments == null || arguments.length == 0)
        ? messagePattern
        : MessageFormatter.arrayFormat(messagePattern, arguments).getMessage();

    String prefix = switch (level) {
      case TRACE -> "[TRACE] ";
      case DEBUG -> "[DEBUG] ";
      default -> "";
    };
    String message = prefix + formatted;

    switch (level) {
      case TRACE, DEBUG, INFO -> {
        if (throwable == null) l.info(message);
        else l.warning(message, throwable);
      }
      case WARN -> {
        if (throwable == null) l.warning(message);
        else l.warning(message, throwable);
      }
      case ERROR -> {
        if (throwable == null) l.severe(message);
        else l.severe(message, throwable);
      }
    }
  }
}
