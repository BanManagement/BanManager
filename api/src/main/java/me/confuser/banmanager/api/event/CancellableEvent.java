package me.confuser.banmanager.api.event;

/**
 * An event that handlers can veto. When {@link #isCancelled()} returns
 * {@code true} after dispatch, the originating action is aborted.
 */
public interface CancellableEvent extends BanManagerEvent {

  /**
   * @return {@code true} if a handler has cancelled this event
   */
  boolean isCancelled();

  /**
   * Mark this event as cancelled, preventing the action from completing.
   */
  void cancel();

  /**
   * Restore the event to its uncancelled state. Useful if a later handler
   * wants to override an earlier cancellation.
   */
  void uncancel();
}
