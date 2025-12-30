package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpBanData;

public class IpBannedEvent extends SilentEvent {
    @Getter
    private IpBanData ban;

    public IpBannedEvent(IpBanData ban, boolean isSilent) {
        super(isSilent);
        this.ban = ban;
    }
}




