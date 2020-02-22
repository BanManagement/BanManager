package me.confuser.banmanager.bungee.api.events;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.plugin.Cancellable;

public abstract class CustomCancellableEvent extends CustomEvent implements Cancellable {

  @Getter
  @Setter
  private boolean cancelled = false;

  public CustomCancellableEvent() {
    super();
  }
}
