package me.confuser.banmanager.bukkit.placeholders;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.util.IPUtils;
import org.bukkit.entity.Player;

public abstract class IpBanPlaceholder extends Placeholder {

  public IpBanPlaceholder(BanManagerPlugin plugin, String identifier) {
    super(plugin, identifier);
  }

  @Override
  public final String getValue(Player player) {
    final IpBanData data = getPlugin().getIpBanStorage().getBan(IPUtils.toIPAddress(player.getAddress().getAddress()));

    if (data == null) return "";

    return getValue(player, data);
  }

  public abstract String getValue(final Player player, final IpBanData data);
}
