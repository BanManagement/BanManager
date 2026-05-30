package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.ReportState;
import me.confuser.banmanager.api.exception.BanManagerException;
import me.confuser.banmanager.api.exception.StorageException;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.storage.ReportStateStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link ReportServiceImpl#states()} / {@code statesSync()} satisfy
 * the async-layer contract:
 * <ul>
 *   <li>{@code states()} returns a {@link CompletableFuture} so callers don't
 *       block the main thread on a workflow-state lookup</li>
 *   <li>{@code statesSync()} returns the same data synchronously for callers
 *       already on a worker thread</li>
 *   <li>{@link SQLException} from the storage layer surfaces as
 *       {@link StorageException} with the contextual error message
 *       ("Failed to load report states") on both variants</li>
 * </ul>
 */
public class ReportServiceImplStatesTest {

  private BanManagerPlugin plugin;
  private ReportStateStorage stateStorage;
  private ReportServiceImpl service;

  @BeforeEach
  public void setUp() {
    plugin = mock(BanManagerPlugin.class);
    stateStorage = mock(ReportStateStorage.class);
    when(plugin.getReportStateStorage()).thenReturn(stateStorage);

    AsyncSupport async = new AsyncSupport(synchronousExecutor());

    service = new ReportServiceImpl(plugin, async);
  }

  @Test
  public void statesSyncMapsInternalRowsToApiDtos() throws Exception {
    when(stateStorage.queryForAll()).thenReturn(Arrays.asList(
        internalState(1, "Open"),
        internalState(2, "Resolved")));

    List<ReportState> result = service.statesSync();

    assertEquals(2, result.size());
    assertEquals(1, result.get(0).id());
    assertEquals("Open", result.get(0).name());
    assertEquals(2, result.get(1).id());
    assertEquals("Resolved", result.get(1).name());
  }

  @Test
  public void statesSyncReturnsEmptyListWhenStorageEmpty() throws Exception {
    when(stateStorage.queryForAll()).thenReturn(Collections.emptyList());

    List<ReportState> result = service.statesSync();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void statesSyncWrapsSqlExceptionWithContextMessage() throws Exception {
    SQLException root = new SQLException("connection refused");
    when(stateStorage.queryForAll()).thenThrow(root);

    StorageException ex = assertThrows(StorageException.class, () -> service.statesSync());

    assertEquals("Failed to load report states", ex.getMessage());
    assertSame(root, ex.getCause());
  }

  @Test
  public void statesAsyncReturnsCompletableFutureWithMappedDtos() throws Exception {
    when(stateStorage.queryForAll()).thenReturn(Collections.singletonList(internalState(7, "Closed")));

    CompletableFuture<List<ReportState>> future = service.states();

    assertNotNull(future, "states() must return a non-null future");
    List<ReportState> result = future.get(5, TimeUnit.SECONDS);
    assertEquals(1, result.size());
    assertEquals(7, result.get(0).id());
    assertEquals("Closed", result.get(0).name());
  }

  @Test
  public void statesAsyncFailsFutureWithStorageExceptionForSqlException() throws Exception {
    SQLException root = new SQLException("driver lost");
    when(stateStorage.queryForAll()).thenThrow(root);

    CompletableFuture<List<ReportState>> future = service.states();

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> future.get(5, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof StorageException,
        "expected StorageException cause; got " + ex.getCause());
    assertEquals("Failed to load report states", ex.getCause().getMessage());
    assertSame(root, ex.getCause().getCause());
  }

  @Test
  public void statesAsyncFailsFutureWithBanManagerExceptionForRuntimeException() throws Exception {
    RuntimeException root = new RuntimeException("unexpected");
    when(stateStorage.queryForAll()).thenThrow(root);

    CompletableFuture<List<ReportState>> future = service.states();

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> future.get(5, TimeUnit.SECONDS));
    // Plain RuntimeException isn't a BanManagerException so AsyncSupport.sync
    // wraps it with the contextual message; the future should still surface
    // it via getCause().
    assertTrue(ex.getCause() instanceof BanManagerException,
        "expected BanManagerException cause; got " + ex.getCause());
    assertEquals("Failed to load report states", ex.getCause().getMessage());
    assertSame(root, ex.getCause().getCause());
  }

  private static me.confuser.banmanager.common.data.ReportState internalState(int id, String name) {
    // ReportState only exposes a generated id from ORMLite, so we set the
    // private field via reflection to keep the test free of a real DB.
    me.confuser.banmanager.common.data.ReportState row =
        new me.confuser.banmanager.common.data.ReportState(name);
    try {
      java.lang.reflect.Field idField = me.confuser.banmanager.common.data.ReportState.class
          .getDeclaredField("id");
      idField.setAccessible(true);
      idField.setInt(row, id);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to set test ReportState id", e);
    }
    return row;
  }

  /**
   * Inline executor so async work runs deterministically on the calling
   * thread; avoids thread-pool teardown plumbing in the test.
   */
  private static Executor synchronousExecutor() {
    return Runnable::run;
  }
}
