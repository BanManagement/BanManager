package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.request.IpBanRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.storage.IpBanStorage;
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
 * Verifies {@link IpBanServiceImpl} honours the sentinel contract: a storage
 * veto on {@code ban} surfaces as {@link Optional#empty()}, and {@code unban}
 * short-circuits to {@code false} without touching storage when no active ban
 * exists for the address.
 */
public class IpBanServiceImplCancellationTest {

  private static final UUID ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private IpBanStorage ipBanStorage;
  private IpBanServiceImpl service;

  @BeforeEach
  public void setUp() throws Exception {
    BanManagerPlugin plugin = mock(BanManagerPlugin.class);
    ipBanStorage = mock(IpBanStorage.class);
    PlayerStorage playerStorage = mock(PlayerStorage.class);
    when(plugin.getIpBanStorage()).thenReturn(ipBanStorage);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);
    when(playerStorage.queryForId(any())).thenReturn(playerEntity(ACTOR, "ModBob"));

    service = new IpBanServiceImpl(plugin, new AsyncSupport(synchronousExecutor()));
  }

  @Test
  public void banSyncReturnsEmptyWhenStorageReportsCancelled() throws Exception {
    when(ipBanStorage.ban(any())).thenReturn(false);

    assertEquals(Optional.empty(), service.banSync(new IpBanRequest(apiIp("198.51.100.7"), ACTOR, "botnet")));
  }

  @Test
  public void asyncBanResolvesToEmptyWhenCancelled() throws Exception {
    when(ipBanStorage.ban(any())).thenReturn(false);

    CompletableFuture<Optional<IpBan>> future =
        service.ban(new IpBanRequest(apiIp("198.51.100.7"), ACTOR, "botnet"));

    assertEquals(Optional.empty(), future.join());
    assertFalse(future.isCompletedExceptionally(),
        "cancellation must resolve to empty, not a failed future");
  }

  @Test
  public void unbanSyncReturnsFalseWhenNoActiveBan() throws Exception {
    when(ipBanStorage.getBan(any(IPAddress.class))).thenReturn(null);

    boolean result = service.unbanSync(apiIp("198.51.100.7"), playerDto(ACTOR, "ModBob"), "appealed", false);

    assertFalse(result);
    verify(ipBanStorage, never()).unban(any(), any(), anyString(), anyBoolean(), anyBoolean());
  }

  @Test
  public void banSyncThrowsOnNullRequest() {
    assertThrows(NullPointerException.class, () -> service.banSync(null));
  }
}
