package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.request.IpMuteRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.storage.IpMuteStorage;
import me.confuser.banmanager.common.storage.PlayerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static me.confuser.banmanager.common.impl.service.ServiceTestFixtures.apiIp;
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
 * Verifies {@link IpMuteServiceImpl} honours the sentinel contract: a storage
 * veto on {@code mute} surfaces as {@link Optional#empty()}, and {@code unmute}
 * short-circuits to {@code false} without touching storage when no active mute
 * exists for the address.
 */
public class IpMuteServiceImplCancellationTest {

  private static final UUID ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private IpMuteStorage ipMuteStorage;
  private IpMuteServiceImpl service;

  @BeforeEach
  public void setUp() throws Exception {
    BanManagerPlugin plugin = mock(BanManagerPlugin.class);
    ipMuteStorage = mock(IpMuteStorage.class);
    PlayerStorage playerStorage = mock(PlayerStorage.class);
    when(plugin.getIpMuteStorage()).thenReturn(ipMuteStorage);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);
    when(playerStorage.queryForId(any())).thenReturn(playerEntity(ACTOR, "ModBob"));

    service = new IpMuteServiceImpl(plugin, new AsyncSupport(synchronousExecutor()));
  }

  @Test
  public void muteSyncReturnsEmptyWhenStorageReportsCancelled() throws Exception {
    when(ipMuteStorage.mute(any())).thenReturn(false);

    assertEquals(Optional.empty(), service.muteSync(new IpMuteRequest(apiIp("198.51.100.7"), ACTOR, "spam")));
  }

  @Test
  public void asyncMuteResolvesToEmptyWhenCancelled() throws Exception {
    when(ipMuteStorage.mute(any())).thenReturn(false);

    CompletableFuture<Optional<IpMute>> future =
        service.mute(new IpMuteRequest(apiIp("198.51.100.7"), ACTOR, "spam"));

    assertEquals(Optional.empty(), future.join());
    assertFalse(future.isCompletedExceptionally(),
        "cancellation must resolve to empty, not a failed future");
  }

  @Test
  public void unmuteSyncReturnsFalseWhenNoActiveMute() throws Exception {
    when(ipMuteStorage.getMute(any(IPAddress.class))).thenReturn(null);

    boolean result = service.unmuteSync(apiIp("198.51.100.7"), playerDto(ACTOR, "ModBob"), "appealed", false);

    assertFalse(result);
    verify(ipMuteStorage, never()).unmute(any(), any(), anyString(), anyBoolean());
  }

  @Test
  public void muteSyncThrowsOnNullRequest() {
    assertThrows(NullPointerException.class, () -> service.muteSync(null));
  }
}
