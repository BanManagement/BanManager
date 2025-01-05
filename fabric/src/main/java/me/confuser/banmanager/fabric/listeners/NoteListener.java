package me.confuser.banmanager.fabric.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonNoteListener;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.common.data.PlayerNoteData;

public class NoteListener {

  private final CommonNoteListener listener;

  public NoteListener(BanManagerPlugin plugin) {
    this.listener = new CommonNoteListener(plugin);

    BanManagerEvents.PLAYER_NOTE_CREATED_EVENT.register(this::notifyOnNote);
  }

  private void notifyOnNote(PlayerNoteData noteData) {
    listener.notifyOnNote(noteData);
  }
}
