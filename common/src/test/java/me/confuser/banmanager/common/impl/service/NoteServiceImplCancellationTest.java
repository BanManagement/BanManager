package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.PlayerNote;
import me.confuser.banmanager.api.request.NoteRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.storage.PlayerNoteStorage;
import me.confuser.banmanager.common.storage.PlayerStorage;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link NoteServiceImpl} surfaces a storage-level veto on
 * {@code addNote} as {@link Optional#empty()} (sync) / a non-exceptional
 * empty future (async).
 */
public class NoteServiceImplCancellationTest {

  private static final UUID PLAYER = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ACTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private PlayerNoteStorage noteStorage;
  private NoteServiceImpl service;

  @BeforeEach
  public void setUp() throws Exception {
    BanManagerPlugin plugin = mock(BanManagerPlugin.class);
    noteStorage = mock(PlayerNoteStorage.class);
    PlayerStorage playerStorage = mock(PlayerStorage.class);
    when(plugin.getPlayerNoteStorage()).thenReturn(noteStorage);
    when(plugin.getPlayerStorage()).thenReturn(playerStorage);
    when(playerStorage.queryForId(any())).thenReturn(playerEntity(PLAYER, "Alice"));

    service = new NoteServiceImpl(plugin, new AsyncSupport(synchronousExecutor()));
  }

  @Test
  public void createSyncReturnsEmptyWhenStorageReportsCancelled() throws Exception {
    when(noteStorage.addNote(any())).thenReturn(false);

    assertEquals(Optional.empty(), service.createSync(new NoteRequest(PLAYER, ACTOR, "alt account")));
  }

  @Test
  public void asyncCreateResolvesToEmptyWhenCancelled() throws Exception {
    when(noteStorage.addNote(any())).thenReturn(false);

    CompletableFuture<Optional<PlayerNote>> future = service.create(new NoteRequest(PLAYER, ACTOR, "alt account"));

    assertEquals(Optional.empty(), future.join());
    assertFalse(future.isCompletedExceptionally(),
        "cancellation must resolve to empty, not a failed future");
  }

  @Test
  public void createSyncThrowsOnNullRequest() {
    assertThrows(NullPointerException.class, () -> service.createSync(null));
  }
}
