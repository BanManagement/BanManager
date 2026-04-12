package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.google.guava.cache.Cache;
import me.confuser.banmanager.common.google.guava.cache.CacheBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ConfirmationManager {

  private static final ConfirmationManager INSTANCE = new ConfirmationManager();

  private final Cache<UUID, PendingAction> pendingActions = CacheBuilder.newBuilder()
      .expireAfterWrite(60, TimeUnit.SECONDS)
      .maximumSize(200)
      .build();

  public static ConfirmationManager getInstance() {
    return INSTANCE;
  }

  public void submit(UUID playerId, String description, Runnable action) {
    pendingActions.put(playerId, new PendingAction(description, action));
  }

  public PendingAction get(UUID playerId) {
    return pendingActions.getIfPresent(playerId);
  }

  /**
   * Atomically retrieve and remove a pending action.
   * Returns null if no action is pending.
   */
  @SuppressWarnings("unchecked")
  public PendingAction getAndRemove(UUID playerId) {
    return (PendingAction) pendingActions.asMap().remove(playerId);
  }

  public boolean cancel(UUID playerId) {
    PendingAction action = pendingActions.getIfPresent(playerId);
    if (action == null) return false;

    pendingActions.invalidate(playerId);
    return true;
  }

  public void clear() {
    pendingActions.invalidateAll();
  }

  public static class PendingAction {
    private final String description;
    private final Runnable action;

    public PendingAction(String description, Runnable action) {
      this.description = description;
      this.action = action;
    }

    public String getDescription() {
      return description;
    }

    public Runnable getAction() {
      return action;
    }
  }
}
