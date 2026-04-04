package me.confuser.banmanager.common.storage.migration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "bm_schema_version")
@NoArgsConstructor
public class SchemaVersion {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(canBeNull = false)
  @Getter
  private int version;

  @DatabaseField(canBeNull = false, columnDefinition = "VARCHAR(255) NOT NULL")
  @Getter
  private String description;

  @DatabaseField(canBeNull = false, columnDefinition = "BIGINT NOT NULL")
  @Getter
  private long appliedAt;

  @DatabaseField(canBeNull = false, columnDefinition = "VARCHAR(50) NOT NULL")
  @Getter
  private String scope;
}
