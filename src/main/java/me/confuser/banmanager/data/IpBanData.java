package me.confuser.banmanager.data;

import me.confuser.banmanager.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class IpBanData {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false, columnDefinition = "INT UNSIGNED NOT NULL")
	private long ip;
	@DatabaseField(canBeNull = false)
	private String reason;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	private PlayerData actor;

	// Should always be database time
	@DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
	private long created = System.currentTimeMillis() / 1000L;
	@DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
	private long updated = System.currentTimeMillis() / 1000L;
	@DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
	private long expires = 0;
	
	IpBanData() {
		
	}
	
	public IpBanData(long ip, PlayerData actor, String reason) {
		this.ip = ip;
		this.reason = reason;
		this.actor = actor;
	}
	
	public IpBanData(long ip, PlayerData actor, String reason, long expires) {
		this.ip = ip;
		this.reason = reason;
		this.actor = actor;
		this.expires = expires;
	}
	
	public IpBanData(long ip, PlayerData actor, String reason, long expires, long created) {
		this.ip = ip;
		this.reason = reason;
		this.actor = actor;
		this.expires = expires;
		this.created = created;
	}
	
	public long getIp() {
		return ip;
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
