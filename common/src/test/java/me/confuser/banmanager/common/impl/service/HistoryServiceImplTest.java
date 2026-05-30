package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.HistoryEntry;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.storage.PlayerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static me.confuser.banmanager.common.impl.service.ServiceTestFixtures.synchronousExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link HistoryServiceImpl} validates pagination up front and
 * returns empty results (without ever hitting the history tables) when the
 * target player is unknown.
 */
public class HistoryServiceImplTest {

  private static final UUID PLAYER = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private BanManagerPlugin plugin;
  private PlayerStorage playerStorage;
  private HistoryServiceImpl service;

  @BeforeEach
  public void setUp() {
    plugin = mock(BanManagerPlugin.class);
    playerStorage = mock(PlayerStorage.class);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);

    service = new HistoryServiceImpl(plugin, new AsyncSupport(synchronousExecutor()));
  }

  @Test
  public void historySyncReturnsEmptyPageWhenPlayerUnknown() throws Exception {
    when(playerStorage.queryForId(any())).thenReturn(null);

    Page<HistoryEntry> page = service.historySync(PLAYER, 0, 10);

    assertTrue(page.items().isEmpty());
    assertEquals(0, page.page());
    assertEquals(10, page.size());
    verify(plugin, never()).getHistoryStorage();
  }

  @Test
  public void namesSyncReturnsEmptyListWhenPlayerUnknown() throws Exception {
    when(playerStorage.queryForId(any())).thenReturn(null);

    assertTrue(service.namesSync(PLAYER).isEmpty());
    verify(plugin, never()).getPlayerHistoryStorage();
  }

  @Test
  public void nameAtSyncReturnsEmptyWhenPlayerUnknown() throws Exception {
    when(playerStorage.queryForId(any())).thenReturn(null);

    assertEquals(Optional.empty(), service.nameAtSync(PLAYER, 1_700_000_000L));
    verify(plugin, never()).getPlayerHistoryStorage();
  }

  @Test
  public void sessionsSyncReturnsEmptyPageWhenPlayerUnknown() throws Exception {
    when(playerStorage.queryForId(any())).thenReturn(null);

    Page<?> page = service.sessionsSync(PLAYER, 0L, 0, 25);

    assertTrue(page.items().isEmpty());
    verify(plugin, never()).getPlayerHistoryStorage();
  }

  @Test
  public void historySyncRejectsNegativePage() {
    assertThrows(IllegalArgumentException.class, () -> service.historySync(PLAYER, -1, 10));
  }

  @Test
  public void historySyncRejectsNonPositiveSize() {
    assertThrows(IllegalArgumentException.class, () -> service.historySync(PLAYER, 0, 0));
  }

  @Test
  public void historySyncRejectsOversizedPage() {
    assertThrows(IllegalArgumentException.class, () -> service.historySync(PLAYER, 0, Integer.MAX_VALUE));
  }

  @Test
  public void sessionsSyncRejectsInvalidPagination() {
    assertThrows(IllegalArgumentException.class, () -> service.sessionsSync(PLAYER, 0L, -1, 10));
    assertThrows(IllegalArgumentException.class, () -> service.sessionsSync(PLAYER, 0L, 0, 0));
  }
}
