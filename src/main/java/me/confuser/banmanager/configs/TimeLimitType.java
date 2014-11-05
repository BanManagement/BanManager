package me.confuser.banmanager.configs;

import lombok.Getter;

public enum TimeLimitType {
	PLAYER_BAN("playerBans"), PLAYER_MUTE("playerMutes"), IP_BAN("ipBans");
	
	@Getter
	private final String name;
	
	private TimeLimitType(String name) {
		this.name = name;
	}
}
