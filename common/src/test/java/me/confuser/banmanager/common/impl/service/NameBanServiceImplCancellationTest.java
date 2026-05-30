package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.request.NameBanRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.storage.NameBanStorage;
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
 * Verifies {@link NameBanServiceImpl} honours the sentinel contract: a
 * storage veto on {@code ban} surfaces as {@link Optional#empty()}, and
 * {@code unban} short-circuits to {@code false} without touching storage
 * when no active name ban exists.
 */
public class NameBanServiceImplCancellationTest {

  private static final String NAME = "BadActor";
  private static final UUID ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private NameBanStorage nameBanStorage;
  private NameBanServiceImpl service;

  @BeforeEach
  public void setUp() throws Exception {
    BanManagerPlugin plugin = mock(BanManagerPlugin.class);
    nameBanStorage = mock(NameBanStorage.class);
    PlayerStorage playerStorage = mock(PlayerStorage.class);
    when(plugin.getNameBanStorage()).thenReturn(nameBanStorage);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);
    when(playerStorage.queryForId(any())).thenReturn(playerEntity(ACTOR, "ModBob"));

    service = new NameBanServiceImpl(plugin, new AsyncSupport(synchronousExecutor()));
  }

  @Test
  public void banSyncReturnsEmptyWhenStorageReportsCancelled() throws Exception {
    when(nameBanStorage.ban(any())).thenReturn(false);

    assertEquals(Optional.empty(), service.banSync(new NameBanRequest(NAME, ACTOR, "impersonation")));
  }

  @Test
  public void asyncBanResolvesToEmptyWhenCancelled() throws Exception {
    when(nameBanStorage.ban(any())).thenReturn(false);

    CompletableFuture<Optional<NameBan>> future = service.ban(new NameBanRequest(NAME, ACTOR, "impersonation"));

    assertEquals(Optional.empty(), future.join());
    assertFalse(future.isCompletedExceptionally(),
        "cancellation must resolve to empty, not a failed future");
  }

  @Test
  public void unbanSyncReturnsFalseWhenNoActiveBan() throws Exception {
    when(nameBanStorage.getBan(anyString())).thenReturn(null);

    boolean result = service.unbanSync(NAME, playerDto(ACTOR, "ModBob"), "appealed", false);

    assertFalse(result);
    verify(nameBanStorage, never()).unban(any(), any(), anyString(), anyBoolean(), anyBoolean());
  }

  @Test
  public void banSyncThrowsOnNullRequest() {
    assertThrows(NullPointerException.class, () -> service.banSync(null));
  }
}
