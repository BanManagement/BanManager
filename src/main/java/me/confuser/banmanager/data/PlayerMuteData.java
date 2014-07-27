package me.confuser.banmanager.data;

import java.util.UUID;

import me.confuser.banmanager.util.UUIDUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerMuteData {
	@DatabaseField(id = true)
	private byte[] id;
	@DatabaseField(canBeNull = false, foreign = true)
	private PlayerData player;
	@DatabaseField(canBeNull = false)
	private String reason;
	@DatabaseField(canBeNull = false, foreign = true)
	private PlayerData actor;
	
	private UUID uuid = null;
	
	// Should always be database time
	@DatabaseField(index = true)
	private long created = System.currentTimeMillis() / 1000;
	@DatabaseField(index = true)
	private long updated = System.currentTimeMillis() / 1000;
	@DatabaseField(index = true)
	private long expires = 0;
	
	PlayerMuteData() {
		
	}
	
	public PlayerMuteData(PlayerData player, PlayerData actor, String reason) {
		uuid = player.getUUID();
		id = UUIDUtils.toBytes(uuid);
		
		this.player = player;
		this.reason = reason;
		this.actor = actor;
	}
	
	public PlayerMuteData(PlayerData player, PlayerData actor, String reason, long expires) {
		uuid = player.getUUID();
		id = UUIDUtils.toBytes(uuid);
		
		this.player = player;
		this.reason = reason;
		this.actor = actor;
		this.expires = expires;
	}
	
	public UUID getUUID() {
		if (uuid == null)
			uuid = UUIDUtils.fromBytes(id);

		return uuid;
	}
	
	public PlayerData getPlayer() {
		return player;
	}
	
	public PlayerData getActor() {
		return actor;
	}

	public long getExpires() {
		return expires;
	}

	public String getReason() {
		return reason;
	}

	public long getCreated() {
		return created;
	}
	
	public boolean hasExpired() {
		return getExpires() <= (System.currentTimeMillis() / 1000);
	}
}
