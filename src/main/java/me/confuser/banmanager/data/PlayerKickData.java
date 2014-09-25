package me.confuser.banmanager.data;

import me.confuser.banmanager.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class PlayerKickData {

      @DatabaseField(generatedId = true)
      private int id;
      @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
      private PlayerData player;
      @DatabaseField(canBeNull = false)
      private String reason;
      @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
      private PlayerData actor;
      // Should always be database time
      @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
      private long created = System.currentTimeMillis() / 1000L;

      public PlayerKickData() {

      }

      public PlayerKickData(PlayerData player, PlayerData actor, String reason) {
            this.player = player;
            this.reason = reason;
            this.actor = actor;
      }

      // Imports only!
      public PlayerKickData(PlayerData player, PlayerData actor, String reason, long created) {
            this.player = player;
            this.reason = reason;
            this.actor = actor;
            this.created = created;
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
