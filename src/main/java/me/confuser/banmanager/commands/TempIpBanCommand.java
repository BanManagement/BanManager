package me.confuser.banmanager.commands;

import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.net.InetAddresses;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;

public class TempIpBanCommand extends BukkitCommand<BanManager> {

      public TempIpBanCommand() {
            super("tempbanip");
      }

      @Override
      public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
            if (args.length < 3) {
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

            long expiresCheck;

            try {
                  expiresCheck = DateUtils.parseDateDiff(args[1], true);
            } catch (Exception e1) {
                  sender.sendMessage(Message.get("invalidTime").toString());
                  return true;
            }

            final long expires = expiresCheck;
            final String reason = StringUtils.join(args, " ", 2, args.length);

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

                              ip = player.getIP();
                        } else {
                              ip = IPUtils.toLong(ipStr);
                        }

                        if (plugin.getIpBanStorage().isBanned(ip)) {
                              Message message = Message.get("ipAlreadyBanned");
                              message.set("ip", ipStr);

                              sender.sendMessage(message.toString());
                              return;
                        }

                        final PlayerData actor;

                        if (sender instanceof Player) {
                              actor = plugin.getPlayerStorage().getOnline((Player) sender);
                        } else {
                              actor = plugin.getPlayerStorage().getConsole();
                        }

                        final IpBanData ban = new IpBanData(ip, actor, reason, expires);
                        boolean created = false;

                        try {
                              created = plugin.getIpBanStorage().ban(ban);
                        } catch (SQLException e) {
                              sender.sendMessage(Message.get("errorOccurred").toString());
                              e.printStackTrace();
                              return;
                        }

                        if (!created) {
                              return;
                        }

                        // Find online players
                        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                              public void run() {
                                    Message kickMessage = Message.get("ipTempBanKick")
                                            .set("reason", ban.getReason())
                                            .set("actor", actor.getName());

                                    for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                                          if (IPUtils.toLong(onlinePlayer.getAddress().getAddress()) == ip) {
                                                onlinePlayer.kickPlayer(kickMessage.toString());
                                          }
                                    }
                              }
                        });

                        Message message = Message.get("ipTempBanned");
                        message
                                .set("ip", ipStr)
                                .set("actor", actor.getName())
                                .set("reason", ban.getReason())
                                .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));

                        plugin.getServer().broadcast(message.toString(), "bm.notify.tempbanip");
                  }

            });

            return true;
      }
}
