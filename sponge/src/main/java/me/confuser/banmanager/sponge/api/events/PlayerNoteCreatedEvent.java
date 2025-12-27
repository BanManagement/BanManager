package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerNoteData;

public class PlayerNoteCreatedEvent extends CustomEvent {
    @Getter
    private PlayerNoteData note;

    public PlayerNoteCreatedEvent(PlayerNoteData note) {
        super();
        this.note = note;
    }
}

