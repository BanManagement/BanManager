package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerMuteData;

public class PlayerMutedEvent extends SilentEvent {

    @Getter
    private PlayerMuteData mute;

    public PlayerMutedEvent(PlayerMuteData mute, boolean isSilent) {
        super(isSilent);
        this.mute = mute;
    }
}


