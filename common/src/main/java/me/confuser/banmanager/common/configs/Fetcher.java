package me.confuser.banmanager.common.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public
class Fetcher {
  @Getter
  private final String url;
  @Getter
  private final String key;
}
