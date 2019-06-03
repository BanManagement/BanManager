package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;

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

  public PlayerReportLocationData(PlayerReportData report, PlayerData player, String world, double x, double y, double z, float pitch, float yaw) {
    this.report = report;
    this.player = player;
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
    this.pitch = pitch;
    this.yaw = yaw;
  }

}
