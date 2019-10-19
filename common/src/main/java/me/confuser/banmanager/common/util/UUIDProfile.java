package me.confuser.banmanager.common.util;

import lombok.Getter;

import java.util.UUID;

public class UUIDProfile {

  @Getter
  private final String name;
  @Getter
  private final UUID uuid;

  public UUIDProfile(String name, UUID uuid) {
    this.name = name;
    this.uuid = uuid;
  }
}
