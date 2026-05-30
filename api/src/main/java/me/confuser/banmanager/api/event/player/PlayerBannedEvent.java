package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Post-event fired after a player ban has been persisted. Carries the
 * immutable {@link PlayerBan} record. Cannot be cancelled.
 *
 * <p>The {@link #silent()} flag may differ from {@code ban.silent()}: a ban
 * that arrived from the global database sync is broadcast as silent when
 * {@code broadcastOnSync = false} is set in config.</p>
 *
 * <p>Handlers may add entries to {@link #placeholders()}; when the ban was
 * triggered against a player who is currently online, BanManager applies the
 * resulting map to the kick-message template before disconnecting them.</p>
 */
public final class PlayerBannedEvent implements BanManagerEvent {

  private final PlayerBan ban;
  private final boolean silent;
  private final Map<String, String> placeholders = new HashMap<>();

  public PlayerBannedEvent(PlayerBan ban, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.silent = silent;
  }

  public PlayerBan ban() { return ban; }
  public boolean silent() { return silent; }

  /**
   * Mutable placeholder map applied to the kick message template when the
   * banned player is online. Add entries like
   * {@code event.placeholders().put("pin", "123456")} to substitute the
   * matching {@code <pin>} token in the rendered message.
   */
  public Map<String, String> placeholders() { return placeholders; }
}
