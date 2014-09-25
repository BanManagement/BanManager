package me.confuser.banmanager.configs;

import java.util.UUID;
import lombok.Getter;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.configs.Config;

public class ConsoleConfig extends Config<BanManager> {

      @Getter
      private String name;
      @Getter
      private UUID uuid;

      public ConsoleConfig() {
            super("console.yml");
      }
      
      @Override
      public void afterLoad() {
            if (conf.getString("uuid", "0").equals("0")) {
                  uuid = UUID.randomUUID();
                  save();
            } else {
                  uuid = UUID.fromString(conf.getString("uuid"));
            }

            name = conf.getString("name");
      }

      @Override
      public void onSave() {
            if (uuid == null) {
                  return;
            }

            conf.set("uuid", uuid.toString());
            conf.set("name", name);
      }
}
