package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.velocity.Cancellable;


public abstract class CustomCancellableEvent extends CustomEvent implements Cancellable {

  @Getter
  @Setter
  private boolean cancelled = false;

  public CustomCancellableEvent() {
    super();
  }
}
