package me.confuser.banmanager.util;

import java.util.UUID;

public class UUIDProfile {
	private final String name;
	private final UUID uuid;
	
	public UUIDProfile(String name, UUID uuid) {
		this.name = name;
		this.uuid = uuid;
	}
	
	public String getName() {
		return name;
	}
	
	public UUID getUUID() {
		return uuid;
	}
}
