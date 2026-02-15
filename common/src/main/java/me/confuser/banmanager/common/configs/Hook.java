package me.confuser.banmanager.common.configs;

import lombok.Getter;

import java.util.List;

public class Hook {

  @Getter
  private final List<ActionCommand> pre;
  @Getter
  private final List<ActionCommand> post;
  @Getter
  private final boolean ignoreSilent;

  public Hook(List<ActionCommand> pre, List<ActionCommand> post) {
    this(pre, post, true);
  }

  public Hook(List<ActionCommand> pre, List<ActionCommand> post, boolean ignoreSilent) {
    this.pre = pre;
    this.post = post;
    this.ignoreSilent = ignoreSilent;
  }
}
