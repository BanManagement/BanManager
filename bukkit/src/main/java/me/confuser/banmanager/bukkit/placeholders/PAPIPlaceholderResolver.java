package me.confuser.banmanager.bukkit.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.PlaceholderResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PAPIPlaceholderResolver implements PlaceholderResolver {
    @Override
    public String resolve(CommonPlayer player, String message) {
        Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
        if (bukkitPlayer == null) return message;
        return PlaceholderAPI.setPlaceholders(bukkitPlayer, message);
    }
}
