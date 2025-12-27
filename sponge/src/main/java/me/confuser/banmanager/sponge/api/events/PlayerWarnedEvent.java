package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerWarnData;

public class PlayerWarnedEvent extends SilentEvent {
    @Getter
    private PlayerWarnData warning;

    public PlayerWarnedEvent(PlayerWarnData warning, boolean isSilent) {
        super(isSilent);
        this.warning = warning;
    }
}



