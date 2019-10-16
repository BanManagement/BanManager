package me.confuser.banmanager.bukkit.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

abstract class CustomEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  public CustomEvent(boolean isAsync) {
    super(isAsync);
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
