package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Post-event fired after an IP-range ban has been persisted.
 *
 * <p>Handlers may add entries to {@link #placeholders()}; when the range ban
 * was triggered against players who are currently connected from a matching
 * IP, BanManager applies the resulting map to the kick-message template
 * before disconnecting them.</p>
 */
public final class IpRangeBannedEvent implements BanManagerEvent {

  private final IpRangeBan ban;
  private final boolean silent;
  private final Map<String, String> placeholders = new HashMap<>();

  public IpRangeBannedEvent(IpRangeBan ban, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.silent = silent;
  }

  public IpRangeBan ban() { return ban; }
  public boolean silent() { return silent; }

  /**
   * Mutable placeholder map applied to the kick message template when
   * matching players are online. See {@link IpRangeBannedEvent} javadoc.
   */
  public Map<String, String> placeholders() { return placeholders; }
}
