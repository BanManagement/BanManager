package me.confuser.banmanager.bukkit.utils;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.config.ConfigKeys;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.util.BukkitUUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.Set;
import java.util.UUID;

public class BukkitCommandUtils {

    private static BanManagerPlugin plugin = BanManager.getPlugin();

    public static Player getPlayer(UUID uuid) {
        if (plugin.getConfiguration().get(ConfigKeys.ONLINEMODE))
            return (Player) plugin.getBootstrap().getPlayer(uuid).orElse(null);

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (BukkitUUIDUtils.getUUID(onlinePlayer).equals(uuid))
                return onlinePlayer;
        }

        return null;
    }

    public static void broadcast(String message, String permission) {

        Set<Permissible> permissibles = Bukkit.getPluginManager().getPermissionSubscriptions("bukkit.broadcast.user");
        for (Permissible permissible : permissibles) {
            if (!(permissible instanceof BlockCommandSender) && (permissible instanceof CommandSender) && permissible
                    .hasPermission(permission)) {
                CommandSender user = (CommandSender) permissible;
                user.sendMessage(message);
            }
        }
    }

}
