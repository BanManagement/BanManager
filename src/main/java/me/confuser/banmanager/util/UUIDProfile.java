package me.confuser.banmanager.util;

import java.util.UUID;
import lombok.Getter;

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
