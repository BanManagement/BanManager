package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class IpBanRecord {
	@DatabaseField(id = true, generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false)
	private long ip;
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
