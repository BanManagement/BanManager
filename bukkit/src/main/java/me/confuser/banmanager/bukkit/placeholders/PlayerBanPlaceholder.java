package me.confuser.banmanager.bukkit.placeholders;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerBanData;
import org.bukkit.entity.Player;

public abstract class PlayerBanPlaceholder extends Placeholder {

  public PlayerBanPlaceholder(BanManagerPlugin plugin, String identifier) {
    super(plugin, identifier);
  }

  @Override
  public final String getValue(Player player) {
    final PlayerBanData data = getPlugin().getPlayerBanStorage().getBan(player.getUniqueId());

    if (data == null) return "";

    return getValue(player, data);
  }

  public abstract String getValue(final Player player, final PlayerBanData data);
}
