package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public abstract class AutoCompleteNameTabCommand<T> extends BukkitCommand<BanManager> implements TabCompleter {

  public AutoCompleteNameTabCommand(String name) {
    super(name);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String commandName, String[] args) {
    ArrayList<String> mostLike = new ArrayList<>();

    if (!sender.hasPermission(command.getPermission())) return mostLike;
    if (args.length != 1) return mostLike;

    for (CharSequence charSequence : plugin.getPlayerStorage().getAutoCompleteTree().getKeysStartingWith(args[0])) {
      mostLike.add(charSequence.toString());
    }

    if (mostLike.size() > 100) return mostLike.subList(0, 99);

    return mostLike;
  }
}
