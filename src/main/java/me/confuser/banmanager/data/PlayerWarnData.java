package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerWarnData {
	@DatabaseField(id = true, generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true)
	private PlayerData player;
	@DatabaseField(canBeNull = false)
	private String reason;
	@DatabaseField(canBeNull = false, foreign = true)
	private PlayerData actor;	
	// Should always be database time
	@DatabaseField(index = true)
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
	
}
