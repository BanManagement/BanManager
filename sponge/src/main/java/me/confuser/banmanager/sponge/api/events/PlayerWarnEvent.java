package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerWarnData;

public class PlayerWarnEvent extends SilentCancellableEvent {
    @Getter
    private PlayerWarnData warning;

    public PlayerWarnEvent(PlayerWarnData warning, boolean isSilent) {
        super(isSilent);
        this.warning = warning;
    }
}




