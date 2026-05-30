package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.request.IpRangeBanRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.storage.IpRangeBanStorage;
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
 * Verifies {@link IpRangeBanServiceImpl} honours the sentinel contract: a
 * storage veto on {@code ban} surfaces as {@link Optional#empty()}, and
 * {@code unban} short-circuits to {@code false} without touching storage when
 * no active range ban matches the supplied range.
 */
public class IpRangeBanServiceImplCancellationTest {

  private static final UUID ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private IpRangeBanStorage rangeBanStorage;
  private IpRangeBanServiceImpl service;

  @BeforeEach
  public void setUp() throws Exception {
    BanManagerPlugin plugin = mock(BanManagerPlugin.class);
    rangeBanStorage = mock(IpRangeBanStorage.class);
    PlayerStorage playerStorage = mock(PlayerStorage.class);
    when(plugin.getIpRangeBanStorage()).thenReturn(rangeBanStorage);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);
    when(playerStorage.queryForId(any())).thenReturn(playerEntity(ACTOR, "ModBob"));

    service = new IpRangeBanServiceImpl(plugin, new AsyncSupport(synchronousExecutor()));
  }

  private static IpRangeBanRequest request() {
    return new IpRangeBanRequest(apiIp("198.51.100.0"), apiIp("198.51.100.255"), ACTOR, "subnet abuse");
  }

  @Test
  public void banSyncReturnsEmptyWhenStorageReportsCancelled() throws Exception {
    when(rangeBanStorage.ban(any())).thenReturn(false);

    assertEquals(Optional.empty(), service.banSync(request()));
  }

  @Test
  public void asyncBanResolvesToEmptyWhenCancelled() throws Exception {
    when(rangeBanStorage.ban(any())).thenReturn(false);

    CompletableFuture<Optional<IpRangeBan>> future = service.ban(request());

    assertEquals(Optional.empty(), future.join());
    assertFalse(future.isCompletedExceptionally(),
        "cancellation must resolve to empty, not a failed future");
  }

  @Test
  public void unbanSyncReturnsFalseWhenNoActiveBan() throws Exception {
    when(rangeBanStorage.getBan(any(IPAddress.class))).thenReturn(null);
    IpRangeBan ban = new IpRangeBan(1, apiIp("198.51.100.0"), apiIp("198.51.100.255"),
        playerDto(ACTOR, "ModBob"), "subnet abuse", 0L, 0L, 0L, false);

    boolean result = service.unbanSync(ban, playerDto(ACTOR, "ModBob"), "appealed", false);

    assertFalse(result);
    verify(rangeBanStorage, never()).unban(any(), any(), anyString(), anyBoolean());
  }

  @Test
  public void banSyncThrowsOnNullRequest() {
    assertThrows(NullPointerException.class, () -> service.banSync(null));
  }
}
