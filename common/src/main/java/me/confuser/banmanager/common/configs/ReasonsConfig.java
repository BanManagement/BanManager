package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class ReasonsConfig extends Config {

  @Getter
  private HashMap<String, String> reasons;

  public ReasonsConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "reasons.yml", logger);
  }

  @Override
  public void afterLoad() {
    reasons = new HashMap<>();

    Set<String> keys = conf.getKeys(false);

    if (keys == null || keys.size() == 0) return;

    for (String reasonKey : keys) {
      String reason = conf.getString(reasonKey);

      if (reasonKey.chars().allMatch(Character::isWhitespace)) continue;

      reasons.put(reasonKey, reason);
    }
  }

  @Override
  public void onSave() {

  }

  public String getReason(String key) {
    return reasons.get(key);
  }
}
