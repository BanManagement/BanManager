package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.PlayerData;

import java.util.UUID;

public final class TargetResolver {

  public enum TargetStatus {
    EXACT_ONLINE,
    EXACT_OFFLINE,
    AMBIGUOUS,
    NOT_FOUND
  }

  public static final class TargetResult {
    private final TargetStatus status;
    private final CommonPlayer onlinePlayer;
    private final String resolvedName;

    private TargetResult(TargetStatus status, CommonPlayer onlinePlayer, String resolvedName) {
      this.status = status;
      this.onlinePlayer = onlinePlayer;
      this.resolvedName = resolvedName;
    }

    public TargetStatus getStatus() {
      return status;
    }

    public CommonPlayer getOnlinePlayer() {
      return onlinePlayer;
    }

    public String getResolvedName() {
      return resolvedName;
    }
  }

  private TargetResolver() {
  }

  public static TargetResult resolveTarget(CommonServer server, String input) {
    if (CommonCommand.isUUID(input)) {
      try {
        CommonPlayer onlinePlayer = server.getPlayer(UUID.fromString(input));

        if (onlinePlayer != null) {
          return new TargetResult(TargetStatus.EXACT_ONLINE, onlinePlayer, onlinePlayer.getName());
        }

        return new TargetResult(TargetStatus.EXACT_OFFLINE, null, input);
      } catch (IllegalArgumentException e) {
        return new TargetResult(TargetStatus.NOT_FOUND, null, input);
      }
    }

    CommonPlayer exactPlayer = server.getPlayerExact(input);
    if (exactPlayer != null) {
      return new TargetResult(TargetStatus.EXACT_ONLINE, exactPlayer, exactPlayer.getName());
    }

    CommonPlayer partialPlayer = server.getPlayer(input);
    if (partialPlayer != null) {
      PlayerData exactStored = BanManagerPlugin.getInstance().getPlayerStorage().retrieve(input, false);

      if (exactStored != null && !exactStored.getName().equalsIgnoreCase(partialPlayer.getName())) {
        return new TargetResult(TargetStatus.AMBIGUOUS, partialPlayer, partialPlayer.getName());
      }

      return new TargetResult(TargetStatus.EXACT_ONLINE, partialPlayer, partialPlayer.getName());
    }

    return new TargetResult(TargetStatus.EXACT_OFFLINE, null, input);
  }
}
