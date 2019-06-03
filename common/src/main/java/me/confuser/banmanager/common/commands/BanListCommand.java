package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.util.IPUtils;

import java.util.List;

public class BanListCommand extends SingleCommand {

  public BanListCommand(LocaleManager locale) {
    super(CommandSpec.BANLIST.localize(locale), "banlist", CommandPermission.BANLIST, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() > 1)
      return CommandResult.INVALID_ARGS;

    String type = "players";

    if (args.size() == 1) {
      type = args.get(0);
    }

    StringBuilder list = new StringBuilder();
    int total = 0;

    if (type.startsWith("play")) {
      if (!sender.hasPermission(this.getPermission().get().getPermission() + ".players")) {
        Message.SENDER_ERROR_NOPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }

      for (PlayerBanData ban : plugin.getPlayerBanStorage().getBans().values()) {
        list.append(ban.getPlayer().getName());
        list.append(", ");

        total++;
      }
    } else if (type.startsWith("ipr")) {
      if (!sender.hasPermission(this.getPermission().get().getPermission() + ".ipranges")) {
        Message.SENDER_ERROR_NOPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }

      for (IpRangeBanData ban : plugin.getIpRangeBanStorage().getBans().values()) {
        list.append(IPUtils.toString(ban.getFromIp()));
        list.append(" - ");
        list.append(IPUtils.toString(ban.getToIp()));
        list.append(", ");

        total++;
      }
    } else if (type.startsWith("ip")) {
      if (!sender.hasPermission(this.getPermission().get().getPermission() + ".ips")) {
        Message.SENDER_ERROR_NOPERMISSION.send(sender);
        return CommandResult.NO_PERMISSION;
      }

      for (IpBanData ban : plugin.getIpBanStorage().getBans().values()) {
        list.append(IPUtils.toString(ban.getIp()));
        list.append(", ");

        total++;
      }
    } else {
      return CommandResult.FAILURE;
    }

    if (list.length() >= 2)
      list.setLength(list.length() - 2);

    Message.BANLIST_HEADER.send(sender, "bans", total);

    if (list.length() > 0)
      sender.sendMessage(list.toString());

    return CommandResult.SUCCESS;
  }
}
