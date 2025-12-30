package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpMuteData;

public class IpMutedEvent extends SilentEvent {
    @Getter
    private IpMuteData mute;

    public IpMutedEvent(IpMuteData mute, boolean isSilent) {
        super(isSilent);
        this.mute = mute;
    }
}




