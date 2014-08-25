package me.confuser.banmanager.events;

import me.confuser.banmanager.data.PlayerMuteData;

public class PlayerUnmuteEvent extends CustomCancellableEvent {
	private PlayerMuteData data;

	public PlayerUnmuteEvent(PlayerMuteData data) {
		this.data = data;
	}
	
	public PlayerMuteData getMute() {
		return data;
	}
}
