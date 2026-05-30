package me.confuser.banmanager.api.event;

/**
 * Marker interface for every event published through {@link EventBus}.
 *
 * <p>Implementations are sealed to the BanManager API module. Plugin authors
 * subscribe via {@link EventBus#subscribe(Class, java.util.function.Consumer)}.</p>
 */
public interface BanManagerEvent {
}
