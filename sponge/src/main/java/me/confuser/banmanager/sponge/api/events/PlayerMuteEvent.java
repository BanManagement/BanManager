package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerMuteData;

public class PlayerMuteEvent extends SilentCancellableEvent {

    @Getter
    private PlayerMuteData mute;

    public PlayerMuteEvent(PlayerMuteData mute, boolean isSilent) {
        super(isSilent);
        this.mute = mute;
    }
}

