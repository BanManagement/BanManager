package me.confuser.banmanager.common.impl.service;

import inet.ipaddr.AddressStringException;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.request.BanRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import me.confuser.banmanager.common.storage.PlayerBanStorage;
import me.confuser.banmanager.common.storage.PlayerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link BanServiceImpl} translates storage-level cancellation
 * (storage returning {@code false} after a pre-event veto) into the documented
 * sentinel values:
 * <ul>
 *   <li>{@code banSync}/{@code ban} resolve to {@link Optional#empty()}</li>
 *   <li>{@code unbanSync}/{@code unban} resolve to {@link Boolean#FALSE}</li>
 *   <li>Neither path completes the future exceptionally</li>
 * </ul>
 *
 * <p>Pre/post event publication itself lives on {@link PlayerBanStorage}; the
 * storage-mock here is configured to mimic that contract without actually
 * publishing.</p>
 */
public class BanServiceImplCancellationTest {

  private static final UUID PLAYER = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID ACTOR = UUID.fromString("44444444-4444-4444-4444-444444444444");

  private BanManagerPlugin plugin;
  private PlayerBanStorage banStorage;
  private PlayerStorage playerStorage;
  private BanServiceImpl service;

  @BeforeEach
  public void setUp() throws Exception {
    plugin = mock(BanManagerPlugin.class);
    banStorage = mock(PlayerBanStorage.class);
    playerStorage = mock(PlayerStorage.class);
    when(plugin.getPlayerBanStorage()).thenReturn(banStorage);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);
    when(playerStorage.queryForId(any())).thenReturn(newPlayer(PLAYER, "Alice"), newPlayer(ACTOR, "ModBob"));

    AsyncSupport async = new AsyncSupport(synchronousExecutor());
    service = new BanServiceImpl(plugin, async);
  }

  // -- ban: storage cancellation surfaces as empty -------------------------

  @Test
  public void banSyncReturnsEmptyWhenStorageReportsCancelled() throws Exception {
    when(banStorage.ban(any())).thenReturn(false);

    Optional<PlayerBan> result = service.banSync(new BanRequest(PLAYER, ACTOR, "spam"));

    assertEquals(Optional.empty(), result);
  }

  @Test
  public void asyncBanResolvesToEmptyWhenStorageReportsCancelled() throws Exception {
    when(banStorage.ban(any())).thenReturn(false);

    CompletableFuture<Optional<PlayerBan>> future =
        service.ban(new BanRequest(PLAYER, ACTOR, "spam"));

    assertEquals(Optional.empty(), future.join());
    assertFalse(future.isCompletedExceptionally(),
        "cancellation must resolve to empty, not a failed future");
  }

  // -- unban: storage cancellation surfaces as false -----------------------

  @Test
  public void unbanSyncReturnsFalseWhenStorageReportsCancelled() throws Exception {
    when(banStorage.getBan(PLAYER)).thenReturn(newBanData());
    when(banStorage.unban(any(), any(), anyString(), anyBoolean(), anyBoolean())).thenReturn(false);

    boolean result = service.unbanSync(PLAYER, newPlayerDto(ACTOR), "appealed", false);

    assertFalse(result);
  }

  @Test
  public void asyncUnbanResolvesToFalseWhenStorageReportsCancelled() throws Exception {
    when(banStorage.getBan(PLAYER)).thenReturn(newBanData());
    when(banStorage.unban(any(), any(), anyString(), anyBoolean(), anyBoolean())).thenReturn(false);

    CompletableFuture<Boolean> future = service.unban(PLAYER, newPlayerDto(ACTOR), "appealed", false);

    assertEquals(Boolean.FALSE, future.join());
    assertFalse(future.isCompletedExceptionally());
  }

  // -- unban: missing-ban early exit ---------------------------------------

  @Test
  public void asyncUnbanResolvesToFalseWhenNoActiveBan() throws java.sql.SQLException {
    when(banStorage.getBan(PLAYER)).thenReturn(null);

    CompletableFuture<Boolean> future = service.unban(PLAYER, newPlayerDto(ACTOR), "appealed", false);

    assertSame(Boolean.FALSE, future.join(),
        "missing-ban early-out should resolve FALSE without invoking storage.unban");
    verify(banStorage, never()).unban(any(), any(), anyString(), anyBoolean(), anyBoolean());
  }

  @Test
  public void unbanSyncReturnsFalseWhenNoActiveBan() {
    when(banStorage.getBan(PLAYER)).thenReturn(null);

    boolean result = service.unbanSync(PLAYER, newPlayerDto(ACTOR), "appealed", false);

    assertFalse(result);
  }

  // -- helpers -------------------------------------------------------------

  private static PlayerData newPlayer(UUID uuid, String name) throws Exception {
    return new PlayerData(uuid, name, new IPAddressString("203.0.113.42").toAddress());
  }

  private static PlayerBanData newBanData() throws Exception {
    return new PlayerBanData(newPlayer(PLAYER, "Alice"), newPlayer(ACTOR, "ModBob"),
        "spam", false, 0L);
  }

  private static Player newPlayerDto(UUID uuid) {
    return new Player(uuid, "ModBob", apiAddress("203.0.113.43"), 1_700_000_000L);
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
   * thread — no thread-pool teardown to fight at @AfterEach time and no risk
   * of a flaky get() timeout under CI load.
   */
  private static Executor synchronousExecutor() {
    return Runnable::run;
  }
}
