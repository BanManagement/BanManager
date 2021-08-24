package me.confuser.banmanager.bungee;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

public class BungeeCommand extends Command implements TabExecutor {
  private CommonCommand command;
  private BMBungeePlugin plugin;

  public BungeeCommand(CommonCommand command, BMBungeePlugin plugin) {
    super(command.getCommandName(), command.getPermission(), command.getAliases().toArray(new String[0]));
    this.command = command;
    this.plugin = plugin;

    register();
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    CommonSender commonSender = getSender(sender);
    boolean success = false;

    try {
      success = this.command.onCommand(commonSender, this.command.getParser(args));
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      e.printStackTrace();
    }

    if (!success) {
      sender.sendMessage(TextComponent.fromLegacyText(command.getUsage()));
    }
  }

  private CommonSender getSender(CommandSender source) {
    if (source instanceof ProxiedPlayer) {
      return new BungeePlayer((ProxiedPlayer) source, BanManagerPlugin.getInstance().getConfig().isOnlineMode());
    } else {
      return new BungeeSender(BanManagerPlugin.getInstance(), source);
    }
  }

  public void register() {
    ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
  }

  @Override
  public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
    if (!this.command.isEnableTabCompletion()) return Collections.emptyList();

    return this.command.handlePlayerNameTabComplete(getSender(commandSender), args);
  }
}
