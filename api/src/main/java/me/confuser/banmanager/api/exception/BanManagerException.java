package me.confuser.banmanager.api.exception;

/**
 * Root unchecked exception for all BanManager API failures.
 *
 * <p>Wraps {@link java.sql.SQLException} and other internal storage failures
 * so the public API never declares a checked exception. Callers may inspect
 * {@link #getCause()} to recover the underlying error.</p>
 */
public class BanManagerException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public BanManagerException(String message) {
    super(message);
  }

  public BanManagerException(String message, Throwable cause) {
    super(message, cause);
  }

  public BanManagerException(Throwable cause) {
    super(cause);
  }
}
