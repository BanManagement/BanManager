package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerReportData;

public class PlayerReportEvent extends SilentCancellableEvent {
    @Getter
    private PlayerReportData report;

    public PlayerReportEvent(PlayerReportData report, boolean isSilent) {
        super(isSilent);
        this.report = report;
    }
}

