package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.PlayerWarn;
import me.confuser.banmanager.api.request.WarnRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.storage.PlayerStorage;
import me.confuser.banmanager.common.storage.PlayerWarnStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static me.confuser.banmanager.common.impl.service.ServiceTestFixtures.playerEntity;
import static me.confuser.banmanager.common.impl.service.ServiceTestFixtures.synchronousExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link WarnServiceImpl} surfaces a storage-level veto on
 * {@code addWarning} as {@link Optional#empty()} (sync) / a non-exceptional
 * empty future (async) rather than completing exceptionally.
 */
public class WarnServiceImplCancellationTest {

  private static final UUID PLAYER = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private PlayerWarnStorage warnStorage;
  private WarnServiceImpl service;

  @BeforeEach
  public void setUp() throws Exception {
    BanManagerPlugin plugin = mock(BanManagerPlugin.class);
    warnStorage = mock(PlayerWarnStorage.class);
    PlayerStorage playerStorage = mock(PlayerStorage.class);
    when(plugin.getPlayerWarnStorage()).thenReturn(warnStorage);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);
    when(playerStorage.queryForId(any())).thenReturn(playerEntity(PLAYER, "Alice"));

    service = new WarnServiceImpl(plugin, new AsyncSupport(synchronousExecutor()));
  }

  @Test
  public void warnSyncReturnsEmptyWhenStorageReportsCancelled() throws Exception {
    when(warnStorage.addWarning(any(), anyBoolean())).thenReturn(false);

    assertEquals(Optional.empty(), service.warnSync(new WarnRequest(PLAYER, ACTOR, "spam")));
  }

  @Test
  public void asyncWarnResolvesToEmptyWhenCancelled() throws Exception {
    when(warnStorage.addWarning(any(), anyBoolean())).thenReturn(false);

    CompletableFuture<Optional<PlayerWarn>> future = service.warn(new WarnRequest(PLAYER, ACTOR, "spam"));

    assertEquals(Optional.empty(), future.join());
    assertFalse(future.isCompletedExceptionally(),
        "cancellation must resolve to empty, not a failed future");
  }

  @Test
  public void warnSyncThrowsOnNullRequest() {
    assertThrows(NullPointerException.class, () -> service.warnSync(null));
  }
}
