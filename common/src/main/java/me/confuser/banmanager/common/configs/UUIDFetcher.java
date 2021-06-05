package me.confuser.banmanager.common.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class UUIDFetcher {
  @Getter
  private final Fetcher idToName;
  @Getter
  private final Fetcher nameToId;
}

