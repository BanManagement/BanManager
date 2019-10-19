package me.confuser.banmanager.common.commands;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.report.*;

public class ReportsCommand extends MultiCommonCommand {

  public ReportsCommand(BanManagerPlugin plugin) {
    super(plugin, "reports");

    registerCommands();
  }

  @Override
  public void registerCommands() {
    registerCommonSubCommand(new AssignSubCommand(getPlugin()));
    registerCommonSubCommand(new CloseSubCommand(getPlugin()));
    registerCommonSubCommand(new InfoSubCommand(getPlugin()));
    registerCommonSubCommand(new ListSubCommand(getPlugin()));
    registerCommonSubCommand(new TeleportSubCommand(getPlugin()));
    registerCommonSubCommand(new UnassignSubCommand(getPlugin()));
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length == 0 && !sender.isConsole()) return getCommands().get("list").onCommand(sender, parser);

    return super.onCommand(sender, parser);
  }


}
