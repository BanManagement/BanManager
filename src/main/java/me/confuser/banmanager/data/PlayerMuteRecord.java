package me.confuser.banmanager.data;

import me.confuser.banmanager.storage.mysql.ByteArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

@DatabaseTable
public class PlayerMuteRecord {

      @DatabaseField(generatedId = true)
      private int id;
      @DatabaseField(canBeNull = false, foreign = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
      @Getter
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

      PlayerMuteRecord() {

      }

      public PlayerMuteRecord(PlayerMuteData mute, PlayerData actor) {
            player = mute.getPlayer();
            reason = mute.getReason();
            expired = mute.getExpires();
            pastActor = mute.getActor();
            pastCreated = mute.getCreated();

            this.actor = actor;
      }

      public PlayerMuteRecord(PlayerMuteData mute, PlayerData actor, long created) {
            player = mute.getPlayer();
            reason = mute.getReason();
            expired = mute.getExpires();
            pastActor = mute.getActor();
            pastCreated = mute.getCreated();

            this.actor = actor;
            this.created = created;
      }
}
