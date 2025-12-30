package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpRangeBanData;

public class IpRangeBanEvent extends SilentCancellableEvent {
    @Getter
    private IpRangeBanData ban;

    public IpRangeBanEvent(IpRangeBanData ban, boolean isSilent) {
        super(isSilent);
        this.ban = ban;
    }
}




