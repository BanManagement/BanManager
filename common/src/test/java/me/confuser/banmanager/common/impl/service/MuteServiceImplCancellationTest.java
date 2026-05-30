package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.request.MuteRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.storage.PlayerMuteStorage;
import me.confuser.banmanager.common.storage.PlayerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static me.confuser.banmanager.common.impl.service.ServiceTestFixtures.playerDto;
import static me.confuser.banmanager.common.impl.service.ServiceTestFixtures.playerEntity;
import static me.confuser.banmanager.common.impl.service.ServiceTestFixtures.synchronousExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link MuteServiceImpl} honours the documented sentinel contract:
 * a storage-level veto surfaces as {@link Optional#empty()} (mute) /
 * {@code false} (unmute) rather than a failed future, and {@code unmute}
 * short-circuits without touching storage when no active mute exists.
 */
public class MuteServiceImplCancellationTest {

  private static final UUID PLAYER = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private PlayerMuteStorage muteStorage;
  private MuteServiceImpl service;

  @BeforeEach
  public void setUp() throws Exception {
    BanManagerPlugin plugin = mock(BanManagerPlugin.class);
    muteStorage = mock(PlayerMuteStorage.class);
    PlayerStorage playerStorage = mock(PlayerStorage.class);
    when(plugin.getPlayerMuteStorage()).thenReturn(muteStorage);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);
    when(playerStorage.queryForId(any())).thenReturn(playerEntity(PLAYER, "Alice"));

    service = new MuteServiceImpl(plugin, new AsyncSupport(synchronousExecutor()));
  }

  @Test
  public void muteSyncReturnsEmptyWhenStorageReportsCancelled() throws Exception {
    when(muteStorage.mute(any())).thenReturn(false);

    assertEquals(Optional.empty(), service.muteSync(new MuteRequest(PLAYER, ACTOR, "spam")));
  }

  @Test
  public void asyncMuteResolvesToEmptyWhenCancelled() throws Exception {
    when(muteStorage.mute(any())).thenReturn(false);

    CompletableFuture<Optional<PlayerMute>> future = service.mute(new MuteRequest(PLAYER, ACTOR, "spam"));

    assertEquals(Optional.empty(), future.join());
    assertFalse(future.isCompletedExceptionally(),
        "cancellation must resolve to empty, not a failed future");
  }

  @Test
  public void unmuteSyncReturnsFalseWhenNoActiveMute() throws Exception {
    when(muteStorage.getMute(any(UUID.class))).thenReturn(null);

    boolean result = service.unmuteSync(PLAYER, playerDto(ACTOR, "ModBob"), "appealed", false);

    assertFalse(result);
    verify(muteStorage, never()).unmute(any(), any(), anyString(), anyBoolean(), anyBoolean());
  }

  @Test
  public void muteSyncThrowsOnNullRequest() {
    assertThrows(NullPointerException.class, () -> service.muteSync(null));
  }
}
