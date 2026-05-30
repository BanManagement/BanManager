package me.confuser.banmanager.api.exception;

/**
 * Reserved exception type for explicit cancellation reporting. The current
 * v8 API <strong>does not throw this exception</strong> — both async and
 * {@code *Sync} variants signal cancellation by returning
 * {@link java.util.Optional#empty()} (for create operations) or
 * {@code false} (for delete operations). Plugin authors should check the
 * returned value rather than wrap the call in a {@code try / catch} for
 * this type. Reserved here so future overloads that need to distinguish
 * cancellation from a routine empty result can promote it without breaking
 * the existing surface.
 */
public class OperationCancelledException extends BanManagerException {

  private static final long serialVersionUID = 1L;

  public OperationCancelledException(String message) {
    super(message);
  }
}
