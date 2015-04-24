package me.confuser.banmanager.configs;

import com.google.common.base.CharMatcher;
import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.configs.Config;

import java.util.HashMap;
import java.util.Set;

public class ReasonsConfig extends Config<BanManager> {
  private HashMap<String, String> reasons;

  public ReasonsConfig() {
    super("reasons.yml");
  }

  @Override
  public void afterLoad() {
    reasons = new HashMap<>();

    Set<String> keys = conf.getKeys(false);

    if (keys == null || keys.size() == 0) return;

    for (String reasonKey : keys) {
      String reason = conf.getString(reasonKey);

      if (CharMatcher.WHITESPACE.matchesAnyOf(reason)) continue;

      reasons.put(reasonKey, reason);
    }
  }

  @Override
  public void onSave() {

  }

  public String getReason(String key) {
    return reasons.get("key");
  }
}
