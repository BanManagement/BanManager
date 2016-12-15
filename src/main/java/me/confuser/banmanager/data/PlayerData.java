package me.confuser.banmanager.data;

import java.net.InetAddress;
import java.util.UUID;

import lombok.Setter;
import me.confuser.banmanager.storage.PlayerStorage;
import me.confuser.banmanager.storage.mysql.ByteArray;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

@DatabaseTable(tableName = "players", daoClass = PlayerStorage.class)
public class PlayerData {

      @DatabaseField(id = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
      @Getter
      private byte[] id;
      @DatabaseField(index = true, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
      @Getter
      @Setter
      private String name;
      @Getter
      @DatabaseField(index = true, columnDefinition = "INT UNSIGNED NOT NULL")
      private long ip;
      @Getter
      @DatabaseField(columnDefinition = "INT(10) NOT NULL")
      private long lastSeen = System.currentTimeMillis() / 1000L;

      private UUID uuid = null;

      PlayerData() {

      }

      public PlayerData(Player player) {
            uuid = UUIDUtils.getUUID(player);
            id = UUIDUtils.toBytes(uuid);
            name = player.getName();
            ip = IPUtils.toLong(player.getAddress().getAddress());
      }

      public PlayerData(UUID uuid, String name) {
            this.uuid = uuid;
            this.id = UUIDUtils.toBytes(uuid);
            this.name = name;
            this.ip = IPUtils.toLong("127.0.0.1");
            this.lastSeen = System.currentTimeMillis() / 1000L;
      }

      public PlayerData(UUID uuid, String name, InetAddress ip) {
            this.uuid = uuid;
            this.id = UUIDUtils.toBytes(uuid);
            this.name = name;
            this.ip = IPUtils.toLong(ip);
            this.lastSeen = System.currentTimeMillis() / 1000L;
      }

      public PlayerData(UUID uuid, String name, long ip, long lastSeen) {
            this.uuid = uuid;
            this.id = UUIDUtils.toBytes(uuid);
            this.name = name;
            this.ip = ip;
            this.lastSeen = lastSeen;
      }

      public UUID getUUID() {
            if (uuid == null) {
                  uuid = UUIDUtils.fromBytes(id);
            }

            return uuid;
      }

      public Player getPlayer() {
            return Bukkit.getPlayer(uuid);
      }
}
