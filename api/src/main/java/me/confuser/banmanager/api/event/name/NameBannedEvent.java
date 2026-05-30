package me.confuser.banmanager.api.event.name;

import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Post-event fired after a name ban has been persisted.
 *
 * <p>Handlers may add entries to {@link #placeholders()}; when the ban was
 * triggered against a player who is currently online with the matching name,
 * BanManager applies the resulting map to the kick-message template before
 * disconnecting them.</p>
 */
public final class NameBannedEvent implements BanManagerEvent {

  private final NameBan ban;
  private final boolean silent;
  private final Map<String, String> placeholders = new HashMap<>();

  public NameBannedEvent(NameBan ban, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.silent = silent;
  }

  public NameBan ban() { return ban; }
  public boolean silent() { return silent; }

  /**
   * Mutable placeholder map applied to the kick message template when the
   * named player is online. See {@link NameBannedEvent} javadoc.
   */
  public Map<String, String> placeholders() { return placeholders; }
}
