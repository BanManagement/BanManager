package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpMuteData;

public class IpMuteEvent extends SilentCancellableEvent {
    @Getter
    private IpMuteData mute;

    public IpMuteEvent(IpMuteData mute, boolean isSilent) {
        super(isSilent);
        this.mute = mute;
    }
}


