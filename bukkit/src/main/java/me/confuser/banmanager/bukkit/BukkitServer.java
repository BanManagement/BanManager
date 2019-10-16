package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonSender;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
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

    return new BukkitPlayer(player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    Player player = Bukkit.getPlayer(name);

    if (player == null) return null;

    return new BukkitPlayer(player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return (CommonPlayer[]) Bukkit.getOnlinePlayers().stream()
                                  .map(player -> new BukkitPlayer(player, plugin.getConfig().isOnlineMode()))
                                  .collect(Collectors.toList()).toArray();
  }

  @Override
  public void broadcast(String message, String permission) {
    Set<Permissible> permissibles = Bukkit.getPluginManager().getPermissionSubscriptions("bukkit.broadcast.user");

    for (Permissible permissible : permissibles) {
      if (!(permissible instanceof BlockCommandSender) && (permissible instanceof CommandSender) && permissible
              .hasPermission(permission)) {
        CommandSender user = (CommandSender) permissible;
        user.sendMessage(message);
      }
    }
  }

  @Override
  public void broadcast(String message, String permission, CommonSender sender) {
    broadcast(message, permission);

    if (!sender.hasPermission(permission)) sender.sendMessage(message);
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
}
