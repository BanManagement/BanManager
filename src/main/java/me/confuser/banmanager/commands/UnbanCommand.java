package me.confuser.banmanager.commands;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;

public class UnbanCommand extends BukkitCommand<BanManager> {

      public UnbanCommand() {
            super("unban");
      }

      @Override
      public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
            if (args.length < 1) {
                  return false;
            }

            // Check if UUID vs name
            final String playerName = args[0];
            final boolean isUUID = playerName.length() > 16;
            boolean isBanned = false;

            if (isUUID) {
                  isBanned = plugin.getPlayerBanStorage().isBanned(UUID.fromString(playerName));
            } else {
                  isBanned = plugin.getPlayerBanStorage().isBanned(playerName);
            }

            if (!isBanned) {
                  Message message = Message.get("notBanned");
                  message.set("player", playerName);

                  sender.sendMessage(message.toString());
                  return true;
            }

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                  @Override
                  public void run() {
                        PlayerBanData ban;

                        if (isUUID) {
                              ban = plugin.getPlayerBanStorage().getBan(UUID.fromString(playerName));
                        } else {
                              ban = plugin.getPlayerBanStorage().getBan(playerName);
                        }

                        if (ban == null) {
                              sender.sendMessage(Message.get("playerNotFound").set("player", playerName).toString());
                              return;
                        }

                        PlayerData actor;

                        if (sender instanceof Player) {
                              actor = plugin.getPlayerStorage().getOnline((Player) sender);
                        } else {
                              actor = plugin.getPlayerStorage().getConsole();
                        }

                        boolean unbanned = false;

                        try {
                              unbanned = plugin.getPlayerBanStorage().unban(ban, actor);
                        } catch (SQLException e) {
                              sender.sendMessage(Message.get("errorOccurred").toString());
                              e.printStackTrace();
                              return;
                        }

                        if (!unbanned) {
                              return;
                        }

                        Message message = Message.get("playerUnbanned");
                        message
                                .set("player", ban.getPlayer().getName())
                                .set("actor", actor.getName());
                        
                        if (!sender.hasPermission("bm.notify.unban")) {
                        	message.sendTo(sender);
                        }

                        plugin.getServer().broadcast(message.toString(), "bm.notify.unban");
                  }

            });

            return true;
      }
}
