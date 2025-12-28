package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerKickData;

public class PlayerKickedEvent extends SilentEvent {
    @Getter
    private PlayerKickData kick;

    public PlayerKickedEvent(PlayerKickData kick, boolean isSilent) {
        super(isSilent);
        this.kick = kick;
    }
}


