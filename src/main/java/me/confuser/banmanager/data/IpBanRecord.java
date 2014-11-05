package me.confuser.banmanager.data;

import me.confuser.banmanager.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

@DatabaseTable
public class IpBanRecord {

	@DatabaseField(generatedId = true)
	@Getter
	private int id;
	@DatabaseField(canBeNull = false, columnDefinition = "INT UNSIGNED NOT NULL")
	@Getter
	private long ip;
	@DatabaseField(canBeNull = false)
	@Getter
	private String reason;
	@DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
	@Getter
	private long expired;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	@Getter
	private PlayerData actor;
	@DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
	@Getter
	private PlayerData pastActor;
	@DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
	@Getter
	private long pastCreated;
	@DatabaseField(canBeNull = false, columnDefinition = "INT(10) NOT NULL")
	@Getter
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

	public IpBanRecord(IpBanData ban, PlayerData actor, long created) {
		ip = ban.getIp();
		reason = ban.getReason();
		expired = ban.getExpires();
		pastActor = ban.getActor();
		pastCreated = ban.getCreated();

		this.actor = actor;
		this.created = created;
	}
}
