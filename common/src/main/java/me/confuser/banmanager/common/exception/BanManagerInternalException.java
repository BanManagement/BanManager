package me.confuser.banmanager.common.exception;

import me.confuser.banmanager.api.exception.BanManagerException;

/**
 * Internal-only equivalent of {@link BanManagerException}. Used inside the
 * common module for runtime errors that have nothing to do with the API
 * surface but still need to bubble up.
 *
 * <p>Extends {@code BanManagerException} so a single {@code catch
 * (BanManagerException)} clause inside the common module reliably traps
 * everything BanManager throws, regardless of whether the failure
 * originated in the public API layer or in internal plumbing.</p>
 */
public class BanManagerInternalException extends BanManagerException {

  public BanManagerInternalException(String message) {
    super(message);
  }

  public BanManagerInternalException(String message, Throwable cause) {
    super(message, cause);
  }
}
