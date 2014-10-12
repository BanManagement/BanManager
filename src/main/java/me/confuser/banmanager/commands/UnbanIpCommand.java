package me.confuser.banmanager.commands;

import java.sql.SQLException;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.net.InetAddresses;

public class UnbanIpCommand extends BukkitCommand<BanManager> {

      public UnbanIpCommand() {
            super("unbanip");
      }

      @Override
      public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
            if (args.length < 1) {
                  return false;
            }

            final String ipStr = args[0];
            final boolean isName = !InetAddresses.isInetAddress(ipStr);

            if (isName && ipStr.length() > 16) {
                  Message message = Message.get("invalidIp");
                  message.set("ip", ipStr);

                  sender.sendMessage(message.toString());
                  return true;
            }

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                  @Override
                  public void run() {
                        final long ip;

                        if (isName) {
                              PlayerData player = plugin.getPlayerStorage().retrieve(ipStr, false);
                              if (player == null) {
                                    sender.sendMessage(Message.get("playerNotFound").set("player", ipStr).toString());
                                    return;
                              }

                              ip = player.getIp();
                        } else {
                              ip = IPUtils.toLong(ipStr);
                        }

                        if (!plugin.getIpBanStorage().isBanned(ip)) {
                              Message message = Message.get("ipNotBanned");
                              message.set("ip", ipStr);

                              sender.sendMessage(message.toString());
                              return;
                        }

                        IpBanData ban = plugin.getIpBanStorage().getBan(ip);

                        PlayerData actor;

                        if (sender instanceof Player) {
                              actor = plugin.getPlayerStorage().getOnline((Player) sender);
                        } else {
                              actor = plugin.getPlayerStorage().getConsole();
                        }

                        boolean unbanned = false;

                        try {
                              unbanned = plugin.getIpBanStorage().unban(ban, actor);
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
                                .set("ip", ipStr)
                                .set("actor", actor.getName());
                        
                        if (!sender.hasPermission("bm.notify.unbanip")) {
                        	message.sendTo(sender);
                        }

                        plugin.getServer().broadcast(message.toString(), "bm.notify.unbanip");
                  }

            });

            return true;
      }
}
