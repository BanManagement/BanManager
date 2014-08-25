package me.confuser.banmanager.events;

import org.bukkit.event.Cancellable;

public class CustomCancellableEvent extends CustomEvent implements Cancellable {
	private boolean cancelled = false;
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean bool) {
		cancelled = bool;
	}
}