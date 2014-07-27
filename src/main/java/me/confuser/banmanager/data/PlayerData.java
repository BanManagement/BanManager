package me.confuser.banmanager.data;

import java.net.InetAddress;
import java.util.UUID;

import me.confuser.banmanager.storage.PlayerStorage;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "players", daoClass = PlayerStorage.class)
public class PlayerData {
	@DatabaseField(id = true)
	private byte[] id;
	@DatabaseField(index = true)
	private String name;
	@DatabaseField(index = true)
	private long ip;
	@DatabaseField
	private long lastSeen = System.currentTimeMillis() / 1000;

	private UUID uuid = null;

	PlayerData() {

	}

	public PlayerData(Player player) {
		uuid = player.getUniqueId();
		id = UUIDUtils.toBytes(uuid);
		name = player.getName();
		ip = IPUtils.toLong(player.getAddress().getAddress());
	}
	
	public PlayerData(UUID uuid, String name) {
		this.uuid = uuid;
		this.id = UUIDUtils.toBytes(uuid);
		this.name = name;
		this.ip = IPUtils.toLong("127.0.0.1");
		this.lastSeen = System.currentTimeMillis() / 1000;
	}
	
	public PlayerData(UUID uuid, String name, InetAddress ip) {
		this.uuid = uuid;
		this.id = UUIDUtils.toBytes(uuid);
		this.name = name;
		this.ip = IPUtils.toLong(ip);
		this.lastSeen = System.currentTimeMillis() / 1000;
	}
	
	public PlayerData(UUID uuid, String name, long ip, long lastSeen) {
		this.uuid = uuid;
		this.id = UUIDUtils.toBytes(uuid);
		this.name = name;
		this.ip = ip;
		this.lastSeen = lastSeen;
	}

	public UUID getUUID() {
		if (uuid == null)
			uuid = UUIDUtils.fromBytes(id);

		return uuid;
	}

	public String getName() {
		return name;
	}

	public long getIP() {
		return ip;
	}

	public long getLastSeen() {
		return lastSeen;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}
}
