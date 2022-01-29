package me.confuser.banmanager.common.data;

import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;
import lombok.Getter;

@DatabaseTable
public class ReportState {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(canBeNull = false)
  @Getter
  private String name;

  ReportState() {

  }

  public ReportState(String name) {
    this.name = name;
  }

}
