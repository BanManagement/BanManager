package me.confuser.banmanager.data;

import java.util.UUID;

import me.confuser.banmanager.storage.mysql.ByteArray;
import me.confuser.banmanager.util.UUIDUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerMuteData {
	@DatabaseField(id = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private byte[] id;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData player;
	@DatabaseField(canBeNull = false)
	private String reason;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData actor;
	
	private UUID uuid = null;
	
	// Should always be database time
	@DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
	private long created = System.currentTimeMillis() / 1000L;
	@DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
	private long updated = System.currentTimeMillis() / 1000L;
	@DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
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
		return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
	}
	
	public long getUpdated() {
		return updated;
	}
}