package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.ormlite.misc.TransactionManager;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Utility class for handling database transactions.
 * Wraps ORMLite's TransactionManager for consistent transaction handling across storage classes.
 * Includes automatic retry logic for InnoDB deadlocks.
 */
public class TransactionHelper {

  // MySQL/MariaDB error codes
  private static final int DEADLOCK_ERROR_CODE = 1213;
  private static final int LOCK_WAIT_TIMEOUT_ERROR_CODE = 1205;

  private static final int MAX_RETRIES = 3;
  private static final int BASE_RETRY_DELAY_MS = 50;

  /**
   * Executes the given callable within a database transaction with deadlock retry logic.
   * If a deadlock or lock wait timeout occurs, the operation will be retried up to MAX_RETRIES times.
   * If the callable throws a non-deadlock exception, the transaction will be rolled back immediately.
   * If the callable completes successfully, the transaction will be committed.
   *
   * @param connectionSource the connection source to use for the transaction
   * @param callable the callable to execute within the transaction
   * @param <T> the return type of the callable
   * @return the result of the callable
   * @throws SQLException if a database error occurs after all retries are exhausted
   */
  public static <T> T callInTransaction(ConnectionSource connectionSource, Callable<T> callable) throws SQLException {
    SQLException lastException = null;

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        return TransactionManager.callInTransaction(connectionSource, callable);
      } catch (SQLException e) {
        if (isRetryableError(e) && attempt < MAX_RETRIES) {
          lastException = e;
          sleepBeforeRetry(attempt);
          continue;
        }
        throw e;
      }
    }

    // Should not reach here, but just in case
    if (lastException != null) {
      throw lastException;
    }

    throw new SQLException("Transaction retry loop exhausted without an exception");
  }

  /**
   * Executes the given runnable within a database transaction with deadlock retry logic.
   * If a deadlock or lock wait timeout occurs, the operation will be retried up to MAX_RETRIES times.
   * If the runnable throws a non-deadlock exception, the transaction will be rolled back immediately.
   * If the runnable completes successfully, the transaction will be committed.
   *
   * @param connectionSource the connection source to use for the transaction
   * @param runnable the runnable to execute within the transaction
   * @throws SQLException if a database error occurs after all retries are exhausted
   */
  public static void runInTransaction(ConnectionSource connectionSource, TransactionRunnable runnable) throws SQLException {
    callInTransaction(connectionSource, () -> {
      runnable.run();
      return null;
    });
  }

  /**
   * Checks if the given SQLException is a retryable error (deadlock or lock wait timeout).
   */
  private static boolean isRetryableError(SQLException e) {
    int errorCode = e.getErrorCode();
    return errorCode == DEADLOCK_ERROR_CODE || errorCode == LOCK_WAIT_TIMEOUT_ERROR_CODE;
  }

  /**
   * Sleeps for an exponentially increasing delay before retrying.
   */
  private static void sleepBeforeRetry(int attempt) {
    try {
      Thread.sleep((long) BASE_RETRY_DELAY_MS * attempt);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Functional interface for runnables that may throw SQLException.
   */
  @FunctionalInterface
  public interface TransactionRunnable {
    void run() throws SQLException;
  }
}
