package me.confuser.banmanager.bukkit.placeholders;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerMuteData;
import org.bukkit.entity.Player;

public abstract class PlayerMutePlaceholder extends Placeholder {

  public PlayerMutePlaceholder(BanManagerPlugin plugin, String identifier) {
    super(plugin, identifier);
  }

  @Override
  public final String getValue(Player player) {
    final PlayerMuteData data = getPlugin().getPlayerMuteStorage().getMute(player.getUniqueId());

    if (data == null) return "";

    return getValue(player, data);
  }

  public abstract String getValue(final Player player, final PlayerMuteData data);
}
