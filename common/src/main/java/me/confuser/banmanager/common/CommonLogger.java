package me.confuser.banmanager.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public interface CommonLogger {
  void info(String s);

  void warning(String s);

  void severe(String s);

  default void severe(String msg, Throwable t) {
    severe(msg);
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    severe(sw.toString());
  }

  default void warning(String msg, Throwable t) {
    warning(msg);
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    warning(sw.toString());
  }
}
