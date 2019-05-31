package me.confuser.banmanager.util.parsers;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Reason {
  @Getter
  @Setter
  private String message;
  @Getter
  private final List<String> notes;

  public Reason(String message, List<String> notes) {
    this.message = message;
    this.notes = notes;
  }
}
