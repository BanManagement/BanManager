package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.PlayerNote;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.NoteRequest;
import me.confuser.banmanager.api.service.NoteService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class NoteServiceImpl implements NoteService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public NoteServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<PlayerNote>> create(NoteRequest request) {
    return async.async(() -> createSync(request));
  }

  @Override
  public Optional<PlayerNote> createSync(NoteRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.player(), "request.player");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.message(), "request.message");

    return AsyncSupport.sync(() -> {
      PlayerData playerEntity = requirePlayer(request.player(), "player");
      PlayerData actorEntity = requirePlayer(request.actor(), "actor");

      PlayerNoteData note = new PlayerNoteData(playerEntity, actorEntity, request.message());

      boolean created = plugin.getPlayerNoteStorage().addNote(note);
      if (!created) {
        return Optional.<PlayerNote>empty();
      }

      return Optional.of(EntityMappers.playerNote(note));
    });
  }

  @Override
  public CompletableFuture<Boolean> delete(int noteId) {
    return async.async(() -> deleteSync(noteId));
  }

  @Override
  public boolean deleteSync(int noteId) {
    return AsyncSupport.sync(
        () -> plugin.getPlayerNoteStorage().deleteById(noteId) > 0,
        "Failed to delete note " + noteId);
  }

  @Override
  public CompletableFuture<Page<PlayerNote>> notes(UUID player, int page, int size) {
    return async.async(() -> notesSync(player, page, size));
  }

  @Override
  public Page<PlayerNote> notesSync(UUID player, int page, int size) {
    return Pagination.recordsByPlayer(
        plugin.getPlayerNoteStorage(),
        plugin.getPlayerStorage(),
        player,
        page,
        size,
        EntityMappers::playerNote);
  }

  private PlayerData requirePlayer(UUID uuid, String label) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No " + label + " player exists with UUID " + uuid);
    }
    return data;
  }
}
