package me.confuser.banmanager.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public abstract class CustomCancellableEvent extends CustomEvent implements Cancellable {

  @Getter
  @Setter
  private boolean cancelled = false;
}
