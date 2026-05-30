package me.confuser.banmanager.api.exception;

/**
 * Thrown when a lookup expects an entity to exist (e.g. {@code unbanSync(playerId)})
 * but no row is found. Callers expecting a possibly-empty result should use
 * the {@code Optional}-returning variants instead.
 */
public class EntityNotFoundException extends BanManagerException {

  private static final long serialVersionUID = 1L;

  public EntityNotFoundException(String message) {
    super(message);
  }

  public EntityNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
