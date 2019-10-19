package me.confuser.banmanager.common.api.events;


import lombok.Getter;

public class CommonEvent {

    @Getter
    private boolean cancelled;
    @Getter
    private boolean silent;

    public CommonEvent(boolean cancelled, boolean silent) {
        this.cancelled = cancelled;
        this.silent = silent;
    }
}
