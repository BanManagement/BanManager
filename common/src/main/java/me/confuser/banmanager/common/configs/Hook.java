package me.confuser.banmanager.common.configs;

import java.util.List;

/**
 * Immutable description of a punishment-event hook (commands to run before
 * and/or after the event fires). Lists are defensively copied so callers
 * cannot mutate the hook's identity after construction.
 */
public record Hook(List<ActionCommand> pre, List<ActionCommand> post, boolean ignoreSilent) {
  public Hook {
    pre = pre == null ? List.of() : List.copyOf(pre);
    post = post == null ? List.of() : List.copyOf(post);
  }

  public Hook(List<ActionCommand> pre, List<ActionCommand> post) {
    this(pre, post, true);
  }
}
