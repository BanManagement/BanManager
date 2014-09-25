package me.confuser.banmanager.storage.conversion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.data.PlayerData;

@AllArgsConstructor
public class PlayerBan {

      @Getter
      @Setter
      private String name;
      @Getter
      @Setter
      private PlayerData actor;
      @Getter
      @Setter
      private String reason;
      @Getter
      @Setter
      private long created;
      @Getter
      @Setter
      private long expires;

}
