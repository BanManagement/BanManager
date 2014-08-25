package me.confuser.banmanager.data;

import me.confuser.banmanager.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerWarnData {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData player;
	@DatabaseField(canBeNull = false)
	private String reason;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData actor;	
	// Should always be database time
	@DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
	private long created = System.currentTimeMillis() / 1000L;
	@DatabaseField(index = true)
	private boolean read = true;
	
	PlayerWarnData() {
		
	}
	
	public PlayerWarnData(PlayerData player, PlayerData actor, String reason) {
		this.player = player;
		this.reason = reason;
		this.actor = actor;
	}
	
	public PlayerWarnData(PlayerData player, PlayerData actor, String reason, boolean read) {
		this.player = player;
		this.reason = reason;
		this.actor = actor;
		this.read = read;
	}
	
	// Imports only!
	public PlayerWarnData(PlayerData player, PlayerData actor, String reason, boolean read, long created) {
		this.player = player;
		this.reason = reason;
		this.actor = actor;
		this.read = read;
		this.created = created;
	}
	
	public PlayerData getPlayer() {
		return player;
	}
	
	public PlayerData getActor() {
		return actor;
	}

	public String getReason() {
		return reason;
	}

	public long getCreated() {
		return created;
	}
	
	public boolean hasRead() {
		return read;
	}

	public void setRead(boolean hasRead) {
		read = hasRead;
	}
	
}
