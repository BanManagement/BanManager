package me.confuser.banmanager.storage.conversion;

public class PlayerProfile {

      public String name;
      public long ip;
      public long lastSeen;

      public PlayerProfile(String name, long ip, long lastSeen) {
            this.name = name;
            this.ip = ip;
            this.lastSeen = lastSeen;
      }
}
