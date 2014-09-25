package me.confuser.banmanager.storage.conversion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.data.PlayerData;

@AllArgsConstructor
public class PlayerRecordBan {

      @Getter
      @Setter
      private String name;
      @Getter
      @Setter
      private PlayerData banActor;
      @Getter
      @Setter
      private String reason;
      @Getter
      @Setter
      private long pastCreated;
      @Getter
      @Setter
      private long expires;
      @Getter
      @Setter
      private PlayerData unbannedActor;
      @Getter
      @Setter
      private long created;

}
