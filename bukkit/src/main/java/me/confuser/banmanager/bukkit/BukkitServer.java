package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitServer implements CommonServer {

  private BanManagerPlugin plugin;

  public BukkitServer() {
  }

  public void enable(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    Player player = Bukkit.getPlayer(uniqueId);

    if (player == null) return null;

    return new BukkitPlayer(plugin, player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    Player player = Bukkit.getPlayer(name);

    if (player == null) return null;

    return new BukkitPlayer(plugin, player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayerExact(String name) {
    Player player = Bukkit.getPlayerExact(name);

    if (player == null) return null;

    return new BukkitPlayer(plugin, player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return Bukkit.getOnlinePlayers().stream()
        .map(player -> new BukkitPlayer(plugin, player, plugin.getConfig().isOnlineMode()))
        .collect(Collectors.toList()).toArray(new CommonPlayer[0]);
  }

  @Override
  public void broadcast(String message, String permission) {
    if(message.isEmpty()) return;

    Set<Permissible> permissibles = Bukkit.getPluginManager().getPermissionSubscriptions("bukkit.broadcast.user");

    for (Permissible permissible : permissibles) {
      if (!(permissible instanceof BlockCommandSender) && (permissible instanceof CommandSender) && permissible
          .hasPermission(permission)) {
        CommandSender user = (CommandSender) permissible;
        user.sendMessage(BukkitServer.formatMessage(message));
      }
    }
  }

  @Override
  public void broadcast(String message, String permission, CommonSender sender) {
    broadcast(message, permission);

    if (!sender.hasPermission(permission)) sender.sendMessage(message);
  }

  public static String formatMessage(String message) {
    return ChatColor.translateAlternateColorCodes('&', ColorUtils.toDownsampledLegacy(message));
  }

  @Override
  public CommonSender getConsoleSender() {
    return new BukkitSender(plugin, Bukkit.getServer().getConsoleSender());
  }

  @Override
  public boolean dispatchCommand(CommonSender sender, String command) {
    CommandSender bukkitSender;

    if (sender.isConsole()) {
      bukkitSender = Bukkit.getServer().getConsoleSender();
    } else {
      bukkitSender = Bukkit.getPlayer(sender.getName());
    }

    return Bukkit.dispatchCommand(bukkitSender, command);
  }

  @Override
  public CommonWorld getWorld(String name) {
    World world = Bukkit.getWorld(name);

    if (world == null) return null;

    return new CommonWorld(name);
  }

  @Override
  public CommonExternalCommand getPluginCommand(String commandName) {
    PluginCommand pluginCommand = Bukkit.getServer().getPluginCommand(commandName);

    if (pluginCommand == null) return null;

    return new CommonExternalCommand(pluginCommand.getPlugin().getDescription().getName().toLowerCase(), pluginCommand.getName(), pluginCommand.getAliases());
  }
}
