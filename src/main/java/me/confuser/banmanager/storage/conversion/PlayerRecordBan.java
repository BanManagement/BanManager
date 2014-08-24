package me.confuser.banmanager.storage.conversion;

import me.confuser.banmanager.data.PlayerData;

public class PlayerRecordBan {
	public String name;
	public PlayerData banActor;
	public String reason;
	public long pastCreated;
	public long expires;
	public PlayerData unbannedActor;
	long created;

	public PlayerRecordBan(String name, PlayerData banActor, String reason, long pastCreated, long expires, PlayerData unbannedActor, long created) {
		this.name = name;
		this.banActor = banActor;
		this.reason = reason;
		this.pastCreated = pastCreated;
		this.expires = expires;
		this.unbannedActor = unbannedActor;
		this.created = created;
	}

}
