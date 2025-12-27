package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpRangeBanData;

public class IpRangeBannedEvent extends SilentEvent {
    @Getter
    private IpRangeBanData ban;

    public IpRangeBannedEvent(IpRangeBanData ban, boolean isSilent) {
        super(isSilent);
        this.ban = ban;
    }
}



