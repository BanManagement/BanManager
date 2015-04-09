package me.confuser.banmanager.data;

import me.confuser.banmanager.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;

@DatabaseTable
public class PlayerWarnData {

      @DatabaseField(generatedId = true)
      @Getter
      private int id;
      @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
      @Getter
      private PlayerData player;
      @DatabaseField(canBeNull = false)
      @Getter
      private String reason;
      @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
      @Getter
      private PlayerData actor;
      // Should always be database time
      @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
      @Getter
      private long created = System.currentTimeMillis() / 1000L;
      @DatabaseField(index = true)
      @Getter
      @Setter
      private boolean read = true;

      PlayerWarnData() {

      }

      public PlayerWarnData(PlayerData player, PlayerData actor, String reason) {
            this.player = player;
            this.reason = reason;
            this.actor = actor;
      }

      public PlayerWarnData(PlayerData player, PlayerData actor, String reason, boolean read) {
            this.player = player;
            this.reason = reason;
            this.actor = actor;
            this.read = read;
      }

      // Imports only!
      public PlayerWarnData(PlayerData player, PlayerData actor, String reason, boolean read, long created) {
            this.player = player;
            this.reason = reason;
            this.actor = actor;
            this.read = read;
            this.created = created;
      }

}
