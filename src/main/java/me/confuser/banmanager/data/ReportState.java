package me.confuser.banmanager.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import me.confuser.banmanager.storage.mysql.ByteArray;

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
