package me.confuser.banmanager.events;

import me.confuser.banmanager.data.PlayerMuteData;

public class PlayerMuteEvent extends CustomCancellableEvent {
	private PlayerMuteData data;

	public PlayerMuteEvent(PlayerMuteData data) {
		this.data = data;
	}
	
	public PlayerMuteData getMute() {
		return data;
	}
}
