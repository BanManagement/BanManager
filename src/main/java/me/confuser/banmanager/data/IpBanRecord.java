package me.confuser.banmanager.data;

import me.confuser.banmanager.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class IpBanRecord {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false, columnDefinition = "INT UNSIGNED NOT NULL")
	private long ip;
	@DatabaseField(canBeNull = false)
	private String reason;
	@DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
	private long expired;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData actor;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData pastActor;
	@DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
	private long pastCreated;
	@DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
	private long created = System.currentTimeMillis() / 1000L;

	IpBanRecord() {
		
	}
	
	public IpBanRecord(IpBanData ban, PlayerData actor) {
		ip = ban.getIp();
		reason = ban.getReason();
		expired = ban.getExpires();
		pastActor = ban.getActor();
		pastCreated = ban.getCreated();
		
		this.actor = actor;
	}
	
	public long getIp() {
		return ip;
	}
}
