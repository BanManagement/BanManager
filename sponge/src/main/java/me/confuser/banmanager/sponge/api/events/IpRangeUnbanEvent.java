package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;

public class IpRangeUnbanEvent extends CustomEvent {
    @Getter
    private IpRangeBanData ban;

    @Getter
    private PlayerData actor;

    @Getter
    private String reason;

    public IpRangeUnbanEvent(IpRangeBanData ban, PlayerData actor, String reason) {
        super();
        this.ban = ban;
        this.actor = actor;
        this.reason = reason;
    }
}


