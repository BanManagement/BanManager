package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Lookup row identifying a report's workflow state. Names are configurable
 * per server but defaults are {@code Open}, {@code Assigned},
 * {@code Resolved}.
 */
public record ReportState(int id, String name) {

  public ReportState {
    Objects.requireNonNull(name, "name");
  }
}
