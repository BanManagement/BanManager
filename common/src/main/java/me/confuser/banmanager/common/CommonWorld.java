package me.confuser.banmanager.common;

import lombok.Getter;

public class CommonWorld {
  @Getter
  private final String name;

  public CommonWorld(String name) {
    this.name = name;
  }
}
