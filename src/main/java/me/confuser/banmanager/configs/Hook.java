package me.confuser.banmanager.configs;

import lombok.Getter;

import java.util.List;

public class Hook {

  @Getter
  private final List<String> pre;
  @Getter
  private final List<String> post;

  public Hook(List<String> pre, List<String> post) {
    this.pre = pre;
    this.post = post;
  }
}
