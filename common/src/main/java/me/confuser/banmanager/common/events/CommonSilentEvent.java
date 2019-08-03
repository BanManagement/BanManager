package me.confuser.banmanager.common.events;

import lombok.Getter;
import lombok.Setter;

public abstract class CommonSilentEvent extends CommonEvent {

  @Getter
  @Setter
  private boolean silent = false;

  public CommonSilentEvent(boolean silent, boolean isAsync) {
    super(isAsync);

    this.silent = silent;
  }
}
