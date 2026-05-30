package me.confuser.banmanager.common.impl.service;

import inet.ipaddr.AddressStringException;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.exception.StorageException;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import me.confuser.banmanager.common.storage.PlayerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the new strict-storage path on {@link PlayerServiceImpl}: prior to
 * the async-layer audit, {@code findByNameSync} and {@code findByIpSync}
 * silently swallowed {@link SQLException} (returning {@code null}/empty list)
 * because they reused the legacy {@code retrieve} / {@code getDuplicatesInTime}
 * helpers. The new wiring routes through
 * {@code PlayerStorage#findByExactName} and {@code findDuplicatesInTime} via
 * {@link AsyncSupport#sync(AsyncSupport.SqlCallable, String)} so failures
 * surface as {@link StorageException} with a meaningful contextual message.
 */
public class PlayerServiceImplPropagationTest {

  private BanManagerPlugin plugin;
  private PlayerStorage playerStorage;
  private PlayerServiceImpl service;

  @BeforeEach
  public void setUp() {
    plugin = mock(BanManagerPlugin.class);
    playerStorage = mock(PlayerStorage.class);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);

    AsyncSupport async = new AsyncSupport(synchronousExecutor());
    service = new PlayerServiceImpl(plugin, async);
  }

  // -- findByNameSync --------------------------------------------------------

  @Test
  public void findByNameSyncReturnsEmptyWhenStorageReturnsNull() throws Exception {
    when(playerStorage.findByExactName("Ghost")).thenReturn(null);

    Optional<Player> result = service.findByNameSync("Ghost");

    assertFalse(result.isPresent(),
        "missing player must surface as Optional.empty(), not null");
  }

  @Test
  public void findByNameSyncReturnsMappedPlayerWhenFound() throws Exception {
    UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(playerStorage.findByExactName("Alice"))
        .thenReturn(newInternalPlayer(uuid, "Alice", "203.0.113.10"));

    Optional<Player> result = service.findByNameSync("Alice");

    assertTrue(result.isPresent());
    assertEquals(uuid, result.get().uuid());
    assertEquals("Alice", result.get().name());
  }

  @Test
  public void findByNameSyncWrapsSqlExceptionWithContextualMessage() throws Exception {
    SQLException root = new SQLException("connection refused");
    when(playerStorage.findByExactName(anyString())).thenThrow(root);

    StorageException ex = assertThrows(StorageException.class,
        () -> service.findByNameSync("Bob"));

    assertEquals("Failed to look up player by name Bob", ex.getMessage());
    assertSame(root, ex.getCause(),
        "original SQLException must be preserved as the cause");
  }

  @Test
  public void findByNameAsyncFailsFutureWithStorageExceptionForSqlException() throws Exception {
    SQLException root = new SQLException("connection refused");
    when(playerStorage.findByExactName(anyString())).thenThrow(root);

    CompletableFuture<Optional<Player>> future = service.findByName("Carol");

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> future.get(5, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof StorageException,
        "expected StorageException; got " + ex.getCause());
    assertEquals("Failed to look up player by name Carol", ex.getCause().getMessage());
    assertSame(root, ex.getCause().getCause());
  }

  // -- findByIpSync ----------------------------------------------------------

  @Test
  public void findByIpSyncMapsPlayersFromStorage() throws Exception {
    UUID uuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
    PlayerData data = newInternalPlayer(uuid, "Dave", "198.51.100.5");
    when(playerStorage.findDuplicatesInTime(any(), anyLong()))
        .thenReturn(Collections.singletonList(data));

    List<Player> matches = service.findByIpSync(apiAddress("198.51.100.5"));

    assertEquals(1, matches.size());
    assertEquals(uuid, matches.get(0).uuid());
    assertEquals("Dave", matches.get(0).name());
  }

  @Test
  public void findByIpSyncReturnsEmptyListWhenNoMatches() throws Exception {
    when(playerStorage.findDuplicatesInTime(any(), anyLong())).thenReturn(Arrays.asList());

    List<Player> matches = service.findByIpSync(apiAddress("198.51.100.5"));

    assertTrue(matches.isEmpty());
  }

  @Test
  public void findByIpSyncWrapsSqlExceptionWithContextualMessage() throws Exception {
    SQLException root = new SQLException("driver lost");
    when(playerStorage.findDuplicatesInTime(any(), anyLong())).thenThrow(root);

    inet.ipaddr.IPAddress ip = apiAddress("203.0.113.99");
    StorageException ex = assertThrows(StorageException.class,
        () -> service.findByIpSync(ip));

    assertTrue(ex.getMessage().startsWith("Failed to look up players by IP "),
        "context message should mention the lookup operation; got: " + ex.getMessage());
    assertTrue(ex.getMessage().contains("203.0.113.99"),
        "context message should include the IP for forensics; got: " + ex.getMessage());
    assertSame(root, ex.getCause());
  }

  @Test
  public void findByIpAsyncFailsFutureWithStorageExceptionForSqlException() throws Exception {
    SQLException root = new SQLException("network");
    when(playerStorage.findDuplicatesInTime(any(), anyLong())).thenThrow(root);

    CompletableFuture<List<Player>> future = service.findByIp(apiAddress("203.0.113.99"));

    ExecutionException ex = assertThrows(ExecutionException.class,
        () -> future.get(5, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof StorageException,
        "expected StorageException; got " + ex.getCause());
    assertSame(root, ex.getCause().getCause());
  }

  // -- helpers ---------------------------------------------------------------

  private static PlayerData newInternalPlayer(UUID uuid, String name, String ip) throws Exception {
    return new PlayerData(uuid, name, new IPAddressString(ip).toAddress());
  }

  private static inet.ipaddr.IPAddress apiAddress(String s) {
    try {
      return new inet.ipaddr.IPAddressString(s).toAddress();
    } catch (AddressStringException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Inline executor so async work runs deterministically on the calling
   * thread; avoids thread-pool teardown plumbing in the test.
   */
  private static Executor synchronousExecutor() {
    return Runnable::run;
  }
}
