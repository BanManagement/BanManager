package me.confuser.banmanager.common.data;

import lombok.Getter;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import me.confuser.banmanager.common.storage.mysql.ByteArray;

@DatabaseTable
public class PlayerReportCommandData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = false, uniqueIndex = false)
  @Getter
  private PlayerReportData report;

  @DatabaseField(index = true, canBeNull = false, foreign = true, foreignAutoRefresh = true, uniqueIndex = false, persisterClass =
          ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private PlayerData actor;

  @DatabaseField(index = true, canBeNull = false)
  @Getter
  private String command;

  @DatabaseField(canBeNull = false)
  @Getter
  private String args;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long updated = System.currentTimeMillis() / 1000L;

  PlayerReportCommandData() {

  }

  public PlayerReportCommandData(PlayerReportData report, PlayerData actor, String command, String args) {
    this.report = report;
    this.actor = actor;
    this.command = command;
    this.args = args;
  }

}
