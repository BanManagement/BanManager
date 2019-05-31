package me.confuser.banmanager.configs;

import lombok.Getter;

import java.util.List;

public class Hook {

  @Getter
  private final List<ActionCommand> pre;
  @Getter
  private final List<ActionCommand> post;

  public Hook(List<ActionCommand> pre, List<ActionCommand> post) {
    this.pre = pre;
    this.post = post;
  }
}
