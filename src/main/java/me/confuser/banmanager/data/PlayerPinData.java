package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;
import org.bukkit.Location;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@DatabaseTable
public class PlayerPinData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = false, persisterClass =
          ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;

  @DatabaseField(canBeNull = false)
  @Getter
  private int pin;

  @DatabaseField(canBeNull = false)
  @Getter
  private long expires;

  PlayerPinData() {

  }

  public PlayerPinData(PlayerData player, Location location) throws NoSuchAlgorithmException {
    this.player = player;
    this.pin = SecureRandom.getInstance("SHA1PRNG").nextInt(900000) + 100000;
    this.expires = (System.currentTimeMillis() / 1000L) + 300; // Valid for 5 minutes
  }

}
