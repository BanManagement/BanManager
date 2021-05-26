package me.confuser.banmanager.bukkit.placeholders;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.util.IPUtils;
import org.bukkit.entity.Player;

public abstract class IpMutePlaceholder extends Placeholder {

  public IpMutePlaceholder(BanManagerPlugin plugin, String identifier) {
    super(plugin, identifier);
  }

  @Override
  public final String getValue(Player player) {
    final IpMuteData data = getPlugin().getIpMuteStorage().getMute(IPUtils.toIPAddress(player.getAddress().getAddress()));

    if (data == null) return "";

    return getValue(player, data);
  }

  public abstract String getValue(final Player player, final IpMuteData data);
}
