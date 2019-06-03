package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.JSONCommandUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FindAltsCommand extends SingleCommand {

  public FindAltsCommand(LocaleManager locale) {
    super(CommandSpec.ALTS.localize(locale), "alts", CommandPermission.ALTS, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 1) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, this.getName(), args);
      return CommandResult.SUCCESS;
    }

    final String ipStr = args.get(0);
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message.SENDER_ERROR_INVALID_IP.send(sender, "ip", ipStr);
      return CommandResult.INVALID_ARGS;
    }

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final long ip;

      if (isName) {
        PlayerData srcPlayer = plugin.getPlayerStorage().retrieve(ipStr, false);
        if (srcPlayer == null) {
          Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", ipStr);
          return;
        }

        ip = srcPlayer.getIp();
      } else {
        ip = IPUtils.toLong(ipStr);
      }

      List<PlayerData> players = plugin.getPlayerStorage().getDuplicates(ip);

      if (!sender.isConsole()) {
        Message.ALTS_HEADER.send(sender, "ip", ipStr);

        if (players.isEmpty()) {
          Message.NONE.send(sender);
          return;
        }

        JSONCommandUtils.alts(players).send((Player) sender);
      } else {
        ArrayList<String> names = new ArrayList<>(players.size());

        for (PlayerData player : players) {
          names.add(player.getName());
        }

        Message.ALTS_HEADER.send(sender, "ip", ipStr);

        if (names.isEmpty()) {
          Message.NONE.send(sender);
          return;
        }

        sender.sendMessage("&6" + StringUtils.join(names, ", "));
      }
    });

    return CommandResult.SUCCESS;
  }

}
