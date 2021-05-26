package me.confuser.banmanager.bukkit.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PAPIPlaceholders extends PlaceholderExpansion {
  private BanManagerPlugin plugin;
  private HashMap<String, Placeholder> placeholders = new HashMap<>();

  public PAPIPlaceholders(BanManagerPlugin plugin) {
    this.plugin = plugin;

    registerPlaceholder("player_bans", (player) -> String.valueOf(plugin.getPlayerBanStorage().getBans().size()));
    registerPlaceholder("player_mutes", (player) -> String.valueOf(plugin.getPlayerMuteStorage().getMutes().size()));
    registerPlaceholder("ip_bans", (player) -> String.valueOf(plugin.getIpBanStorage().getBans().size()));
    registerPlaceholder("ip_mutes", (player) -> String.valueOf(plugin.getIpMuteStorage().getMutes().size()));
    registerPlaceholder("iprange_bans", (player) -> String.valueOf(plugin.getIpRangeBanStorage().getBans().size()));

    registerBanPlaceholder("currentban_id", (player, data) -> String.valueOf(data.getId()));
    registerBanPlaceholder("currentban_created", (player, data) -> String.valueOf(data.getCreated()));
    registerBanPlaceholder("currentban_expires", (player, data) -> String.valueOf(data.getExpires()));
    registerBanPlaceholder("currentban_reason", (player, data) -> data.getReason());
    registerBanPlaceholder("currentban_actor_id", (player, data) -> data.getActor().getUUID().toString());
    registerBanPlaceholder("currentban_actor_name", (player, data) -> data.getActor().getName());

    registerMutePlaceholder("currentmute_id", (player, data) -> String.valueOf(data.getId()));
    registerMutePlaceholder("currentmute_created", (player, data) -> String.valueOf(data.getCreated()));
    registerMutePlaceholder("currentmute_expires", (player, data) -> String.valueOf(data.getExpires()));
    registerMutePlaceholder("currentmute_reason", (player, data) -> data.getReason());
    registerMutePlaceholder("currentmute_actor_id", (player, data) -> data.getActor().getUUID().toString());
    registerMutePlaceholder("currentmute_actor_name", (player, data) -> data.getActor().getName());

    registerIpBanPlaceholder("currentipban_id", (player, data) -> String.valueOf(data.getId()));
    registerIpBanPlaceholder("currentipban_created", (player, data) -> String.valueOf(data.getCreated()));
    registerIpBanPlaceholder("currentipban_expires", (player, data) -> String.valueOf(data.getExpires()));
    registerIpBanPlaceholder("currentipban_reason", (player, data) -> data.getReason());
    registerIpBanPlaceholder("currentipban_actor_id", (player, data) -> data.getActor().getUUID().toString());
    registerIpBanPlaceholder("currentipban_actor_name", (player, data) -> data.getActor().getName());
    registerIpBanPlaceholder("currentipban_ip", (player, data) -> data.getIp().toString());

    registerIpMutePlaceholder("currentipmute_id", (player, data) -> String.valueOf(data.getId()));
    registerIpMutePlaceholder("currentipmute_created", (player, data) -> String.valueOf(data.getCreated()));
    registerIpMutePlaceholder("currentipmute_expires", (player, data) -> String.valueOf(data.getExpires()));
    registerIpMutePlaceholder("currentipmute_reason", (player, data) -> data.getReason());
    registerIpMutePlaceholder("currentipmute_actor_id", (player, data) -> data.getActor().getUUID().toString());
    registerIpMutePlaceholder("currentipmute_actor_name", (player, data) -> data.getActor().getName());
    registerIpMutePlaceholder("currentipmute_ip", (player, data) -> data.getIp().toString());
  }

  public void registerPlaceholder(String identifier, final Function<Player, String> fn) {
    placeholders.put(identifier, new Placeholder(plugin, identifier) {
      @Override
      public String getValue(final Player player) {
        return fn.apply(player);
      }
    });
  }

  public void registerBanPlaceholder(String identifier, final BiFunction<Player, PlayerBanData, String> fn) {
    placeholders.put(identifier, new PlayerBanPlaceholder(plugin, identifier) {
      @Override
      public String getValue(final Player player, final PlayerBanData data) {
        return fn.apply(player, data);
      }
    });
  }

  public void registerMutePlaceholder(String identifier, final BiFunction<Player, PlayerMuteData, String> fn) {
    placeholders.put(identifier, new PlayerMutePlaceholder(plugin, identifier) {
      @Override
      public String getValue(final Player player, final PlayerMuteData data) {
        return fn.apply(player, data);
      }
    });
  }

  public void registerIpBanPlaceholder(String identifier, final BiFunction<Player, IpBanData, String> fn) {
    placeholders.put(identifier, new IpBanPlaceholder(plugin, identifier) {
      @Override
      public String getValue(final Player player, final IpBanData data) {
        return fn.apply(player, data);
      }
    });
  }

  public void registerIpMutePlaceholder(String identifier, final BiFunction<Player, IpMuteData, String> fn) {
    placeholders.put(identifier, new IpMutePlaceholder(plugin, identifier) {
      @Override
      public String getValue(final Player player, final IpMuteData data) {
        return fn.apply(player, data);
      }
    });
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public String getAuthor() {
    return "confuser";
  }

  @Override
  public String getIdentifier() {
    return "bm";
  }

  @Override
  public String getVersion() {
    return "7";
  }

  @Override
  public String onPlaceholderRequest(Player player, String identifier) {
    Placeholder placeholder = placeholders.get(identifier);

    if (placeholder == null) {
      plugin.getLogger().warning("Unknown placeholder " + identifier + " detected");
      return null;
    }

    return placeholder.getValue(player);
  }
}