package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;
import org.bukkit.Location;

@DatabaseTable
public class PlayerReportLocationData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = false)
  @Getter
  private PlayerReportData report;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = false, persisterClass =
          ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData player;

  @DatabaseField(index = true, canBeNull = false)
  @Getter
  private String world;

  @DatabaseField(canBeNull = false)
  @Getter
  private double x;

  @DatabaseField(canBeNull = false)
  @Getter
  private double y;

  @DatabaseField(canBeNull = false)
  @Getter
  private double z;

  @DatabaseField(canBeNull = false)
  @Getter
  private float pitch;

  @DatabaseField(canBeNull = false)
  @Getter
  private float yaw;

  PlayerReportLocationData() {

  }

  public PlayerReportLocationData(PlayerReportData report, PlayerData player, Location location) {
    this.report = report;
    this.player = player;
    this.world = location.getWorld().getName();
    this.x = location.getX();
    this.y = location.getY();
    this.z = location.getZ();
    this.pitch = location.getPitch();
    this.yaw = location.getYaw();
  }

}
