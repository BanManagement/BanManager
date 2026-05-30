package me.confuser.banmanager.api.event.player;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Fired when BanManager would deny a login or join attempt (banned UUID,
 * banned IP, range ban, name ban). Allows administrative monitoring of
 * blocked attempts.
 *
 * <p>Handlers may add entries to {@link #placeholders()}; BanManager applies
 * them to the kick message template (e.g. {@code ban.player.disallowed})
 * before rendering and disconnecting the player. Companion plugins use this
 * hook to inject custom placeholders such as {@code <pin>} into kick
 * messages.</p>
 */
public final class PlayerDeniedEvent implements BanManagerEvent {

  /**
   * Type of denial.
   */
  public enum Reason {
    PLAYER_BAN, IP_BAN, IP_RANGE_BAN, NAME_BAN
  }

  private final Optional<UUID> uuid;
  private final String name;
  private final Optional<IPAddress> ip;
  private final Reason reason;
  private final Map<String, String> placeholders = new HashMap<>();

  public PlayerDeniedEvent(Optional<UUID> uuid, String name, Optional<IPAddress> ip, Reason reason) {
    this.uuid = Objects.requireNonNull(uuid, "uuid");
    this.name = Objects.requireNonNull(name, "name");
    this.ip = Objects.requireNonNull(ip, "ip");
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  public Optional<UUID> uuid() { return uuid; }
  public String name() { return name; }
  public Optional<IPAddress> ip() { return ip; }
  public Reason reason() { return reason; }

  /**
   * Mutable placeholder map applied to the kick message template by
   * BanManager after this event is published. Add entries like
   * {@code event.placeholders().put("pin", "123456")} to substitute the
   * matching {@code <pin>} token in the rendered message.
   *
   * <p><strong>Concurrency:</strong> the map is the live event-shared
   * instance — every subscriber observes a single shared map. When two
   * subscribers write the same key, last-writer-wins; ordering follows the
   * registered {@link me.confuser.banmanager.api.event.EventPriority}.
   * Subscribers should namespace their keys (e.g. {@code "wenh.pin"}) to
   * avoid stomping placeholders set by other plugins. Callers should
   * <strong>not</strong> retain a reference to the map after the handler
   * returns — BanManager re-uses it during render and may resize it.</p>
   */
  public Map<String, String> placeholders() { return placeholders; }
}
