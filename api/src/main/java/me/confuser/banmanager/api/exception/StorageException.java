package me.confuser.banmanager.api.exception;

/**
 * Thrown when the underlying storage layer fails. Always carries the original
 * {@link java.sql.SQLException} (or driver exception) as its {@link #getCause()}.
 */
public class StorageException extends BanManagerException {

  private static final long serialVersionUID = 1L;

  public StorageException(String message, Throwable cause) {
    super(message, cause);
  }

  public StorageException(Throwable cause) {
    super(cause);
  }
}
