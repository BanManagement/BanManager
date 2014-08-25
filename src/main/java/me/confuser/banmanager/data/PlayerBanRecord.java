package me.confuser.banmanager.data;

import me.confuser.banmanager.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerBanRecord {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData player;
	@DatabaseField(canBeNull = false)
	private String reason;
	@DatabaseField(canBeNull = false)
	private long expired;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData actor;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData pastActor;
	@DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
	private long pastCreated;
	@DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
	private long created = System.currentTimeMillis() / 1000L;

	PlayerBanRecord() {
		
	}
	
	public PlayerBanRecord(PlayerBanData ban, PlayerData actor) {
		player = ban.getPlayer();
		reason = ban.getReason();
		expired = ban.getExpires();
		pastActor = ban.getActor();
		pastCreated = ban.getCreated();
		
		this.actor = actor;
	}
	
	public PlayerBanRecord(PlayerBanData ban, PlayerData actor, long created) {
		player = ban.getPlayer();
		reason = ban.getReason();
		expired = ban.getExpires();
		pastActor = ban.getActor();
		pastCreated = ban.getCreated();
		
		this.actor = actor;
		this.created = created;
	}
	
	public PlayerData getPlayer() {
		return player;
	}
}
