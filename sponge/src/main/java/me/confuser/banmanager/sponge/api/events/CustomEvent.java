package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.plugin.PluginContainer;

public abstract class CustomEvent implements Event {
    @Getter
    private final Cause cause;

    public CustomEvent() {
        PluginContainer plugin = Sponge.pluginManager().plugin("banmanager").orElseThrow();
        EventContext eventContext = EventContext.builder()
            .add(EventContextKeys.PLUGIN, plugin)
            .build();

        this.cause = Cause.of(eventContext, plugin);
    }

    @Override
    public Cause cause() {
        return cause;
    }
}
