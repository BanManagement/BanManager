package me.confuser.banmanager.storage.conversion;

import me.confuser.banmanager.data.PlayerData;

public class PlayerBan {
	public String name;
	public PlayerData actor;
	public String reason;
	public long created;
	public long expires;

	
	public PlayerBan(String name, PlayerData actor, String reason, long created, long expires) {
		this.name = name;
		this.actor = actor;
		this.reason = reason;
		this.created = created;
		this.expires = expires;
	}
}
