package me.confuser.banmanager.events;

import me.confuser.banmanager.data.PlayerWarnData;

public class PlayerWarnEvent extends CustomCancellableEvent {
	private PlayerWarnData data;
	
	public PlayerWarnEvent(PlayerWarnData data) {
		this.data = data;
	}
	
	public PlayerWarnData getWarning() {
		return data;
	}
}
