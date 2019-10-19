package me.confuser.banmanager.common.commands;

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
