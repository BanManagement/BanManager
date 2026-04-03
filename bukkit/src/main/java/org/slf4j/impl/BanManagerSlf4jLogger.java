package org.slf4j.impl;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonLogger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * SLF4J logger that delegates to BanManager's {@link CommonLogger}.
 *
 * <p>Level filtering: when debug mode is off, TRACE / DEBUG / INFO are
 * suppressed. This replaces the old {@code disableDatabaseLogging()} system
 * property which has no effect when ORMLite uses SLF4J instead of LocalLog.</p>
 */
public class BanManagerSlf4jLogger extends MarkerIgnoringBase {

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

  // --- Level checks -----------------------------------------------------------

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

  // --- TRACE ------------------------------------------------------------------

  @Override
  public void trace(String msg) {
    if (!isTraceEnabled()) return;
    CommonLogger l = logger();
    if (l != null) l.info("[TRACE] " + msg);
  }

  @Override
  public void trace(String format, Object arg) {
    if (!isTraceEnabled()) return;
    FormattingTuple ft = MessageFormatter.format(format, arg);
    trace(ft.getMessage());
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    if (!isTraceEnabled()) return;
    FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
    trace(ft.getMessage());
  }

  @Override
  public void trace(String format, Object... arguments) {
    if (!isTraceEnabled()) return;
    FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
    trace(ft.getMessage());
  }

  @Override
  public void trace(String msg, Throwable t) {
    if (!isTraceEnabled()) return;
    CommonLogger l = logger();
    if (l != null) l.warning("[TRACE] " + msg, t);
  }

  // --- DEBUG ------------------------------------------------------------------

  @Override
  public void debug(String msg) {
    if (!isDebugEnabled()) return;
    CommonLogger l = logger();
    if (l != null) l.info("[DEBUG] " + msg);
  }

  @Override
  public void debug(String format, Object arg) {
    if (!isDebugEnabled()) return;
    FormattingTuple ft = MessageFormatter.format(format, arg);
    debug(ft.getMessage());
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    if (!isDebugEnabled()) return;
    FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
    debug(ft.getMessage());
  }

  @Override
  public void debug(String format, Object... arguments) {
    if (!isDebugEnabled()) return;
    FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
    debug(ft.getMessage());
  }

  @Override
  public void debug(String msg, Throwable t) {
    if (!isDebugEnabled()) return;
    CommonLogger l = logger();
    if (l != null) l.warning("[DEBUG] " + msg, t);
  }

  // --- INFO -------------------------------------------------------------------

  @Override
  public void info(String msg) {
    if (!isInfoEnabled()) return;
    CommonLogger l = logger();
    if (l != null) l.info(msg);
  }

  @Override
  public void info(String format, Object arg) {
    if (!isInfoEnabled()) return;
    FormattingTuple ft = MessageFormatter.format(format, arg);
    info(ft.getMessage());
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    if (!isInfoEnabled()) return;
    FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
    info(ft.getMessage());
  }

  @Override
  public void info(String format, Object... arguments) {
    if (!isInfoEnabled()) return;
    FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
    info(ft.getMessage());
  }

  @Override
  public void info(String msg, Throwable t) {
    if (!isInfoEnabled()) return;
    CommonLogger l = logger();
    if (l != null) l.warning(msg, t);
  }

  // --- WARN -------------------------------------------------------------------

  @Override
  public void warn(String msg) {
    CommonLogger l = logger();
    if (l != null) l.warning(msg);
  }

  @Override
  public void warn(String format, Object arg) {
    FormattingTuple ft = MessageFormatter.format(format, arg);
    warn(ft.getMessage());
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
    warn(ft.getMessage());
  }

  @Override
  public void warn(String format, Object... arguments) {
    FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
    warn(ft.getMessage());
  }

  @Override
  public void warn(String msg, Throwable t) {
    CommonLogger l = logger();
    if (l != null) l.warning(msg, t);
  }

  // --- ERROR ------------------------------------------------------------------

  @Override
  public void error(String msg) {
    CommonLogger l = logger();
    if (l != null) l.severe(msg);
  }

  @Override
  public void error(String format, Object arg) {
    FormattingTuple ft = MessageFormatter.format(format, arg);
    error(ft.getMessage());
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
    error(ft.getMessage());
  }

  @Override
  public void error(String format, Object... arguments) {
    FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
    error(ft.getMessage());
  }

  @Override
  public void error(String msg, Throwable t) {
    CommonLogger l = logger();
    if (l != null) l.severe(msg, t);
  }
}
