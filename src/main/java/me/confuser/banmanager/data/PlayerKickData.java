package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerKickData {
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
	
	PlayerKickData() {
		
	}
	
	public PlayerKickData(PlayerData player, PlayerData actor, String reason) {
		this.player = player;
		this.reason = reason;
		this.actor = actor;
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
}
