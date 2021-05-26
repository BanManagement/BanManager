package me.confuser.banmanager.bukkit.placeholders;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import org.bukkit.entity.Player;

public abstract class Placeholder {
  @Getter
  private final BanManagerPlugin plugin;
  @Getter
  private final String identifier;

  public Placeholder(BanManagerPlugin plugin, String identifier) {
    this.plugin = plugin;
    this.identifier = identifier;
  }

  public abstract String getValue(final Player player);
}
