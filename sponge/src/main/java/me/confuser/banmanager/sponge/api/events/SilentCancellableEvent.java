package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.event.Cancellable;

public abstract class SilentCancellableEvent extends CustomEvent implements Cancellable {
    @Getter
    @Setter
    private boolean cancelled = false;

    @Getter
    @Setter
    private boolean silent;

    public SilentCancellableEvent(boolean silent) {
        super();
        this.silent = silent;
    }
}

