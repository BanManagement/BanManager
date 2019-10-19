package me.confuser.banmanager.common.configs;

import lombok.Getter;

public class CleanUp {

  @Getter
  private int days = 0;
  @Getter
  private long millis = 0;

  public CleanUp(int days) {
    this.days = days;
    this.millis = days * 86400;
  }
}