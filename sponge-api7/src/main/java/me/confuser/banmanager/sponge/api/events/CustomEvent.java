package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.plugin.PluginContainer;

public abstract class CustomEvent extends AbstractEvent {
    @Getter
    private final Cause cause;

    public CustomEvent() {
        PluginContainer plugin = Sponge.getPluginManager().getPlugin("banmanager").get();
        EventContext eventContext = EventContext.builder().add(EventContextKeys.PLUGIN, plugin).build();

        this.cause = Cause.of(eventContext, plugin);
    }
}
