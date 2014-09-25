package me.confuser.banmanager.storage.conversion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class PlayerProfile {

      @Getter
      @Setter
      private String name;
      @Getter
      @Setter
      private long ip;
      @Getter
      @Setter
      private long lastSeen;
}
