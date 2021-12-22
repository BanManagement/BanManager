package me.confuser.banmanager.velocity;

/**
 * Dummy class which all callable events must extend.
 */
public abstract class Event
{

    /**
     * Method called after this event has been dispatched to all handlers.
     */
    public void postCall() {}
}