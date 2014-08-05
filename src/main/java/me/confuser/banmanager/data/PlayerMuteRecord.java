package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerMuteRecord {
	@DatabaseField(id = true, generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true)
	private PlayerData player;
	@DatabaseField(canBeNull = false)
	private String reason;
	@DatabaseField(canBeNull = false)
	private long expired;
	@DatabaseField(canBeNull = false, foreign = true)
	private PlayerData actor;
	@DatabaseField(canBeNull = false, foreign = true)
	private PlayerData pastActor;
	@DatabaseField(canBeNull = false)
	private long pastCreated;
	@DatabaseField(canBeNull = false)
	private long created = System.currentTimeMillis() / 1000L;

	PlayerMuteRecord() {
		
	}
	
	public PlayerMuteRecord(PlayerMuteData mute, PlayerData actor) {
		player = mute.getPlayer();
		reason = mute.getReason();
		expired = mute.getExpires();
		pastActor = mute.getActor();
		pastCreated = mute.getCreated();
		
		this.actor = actor;
	}
	
	public PlayerData getPlayer() {
		return player;
	}
}
