package me.confuser.banmanager.velocity;
// This totally isn't stolen from https://github.com/SpigotMC/BungeeCord/blob/master/api/src/main/java/net/md_5/bungee/api/plugin/Cancellable.java
/**
 * Events that implement this indicate that they may be cancelled and thus
 * prevented from happening.
 */
public interface Cancellable {

    /**
     * Get whether or not this event is cancelled.
     *
     * @return the cancelled state of this event
     */
    public boolean isCancelled();

    /**
     * Sets the cancelled state of this event.
     *
     * @param cancel the state to set
     */
    public void setCancelled(boolean cancel);
}