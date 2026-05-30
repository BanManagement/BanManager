package me.confuser.banmanager.api.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.PlayerNote;
import me.confuser.banmanager.api.request.NoteRequest;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Player note operations. {@code create} fires a cancellable
 * {@link me.confuser.banmanager.api.event.player.PlayerNoteEvent}; cancel
 * surfaces as {@link Optional#empty()} on both async and sync paths.
 * {@code delete} does not fire a pre-event.
 */
public interface NoteService {

  CompletableFuture<Optional<PlayerNote>> create(NoteRequest request);

  /** Returns empty when the pre-event was cancelled or the note could not be persisted. */
  Optional<PlayerNote> createSync(NoteRequest request);

  CompletableFuture<Boolean> delete(int noteId);

  boolean deleteSync(int noteId);

  CompletableFuture<Page<PlayerNote>> notes(UUID player, int page, int size);

  Page<PlayerNote> notesSync(UUID player, int page, int size);
}
