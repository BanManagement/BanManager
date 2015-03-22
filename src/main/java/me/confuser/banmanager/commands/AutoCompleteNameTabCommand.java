package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      String[] names = CommandUtils.splitNameDelimiter(args[0]);

      String lookup = names[names.length - 1];

      if (plugin.getConfiguration().isOfflineAutoComplete()) {
        for (CharSequence charSequence : plugin.getPlayerStorage().getAutoCompleteTree().getKeysStartingWith(lookup)) {
          mostLike.add(args[0] + charSequence.toString().substring(lookup.length()));
        }
      } else {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
          if (player.getName().toLowerCase().startsWith(lookup.toLowerCase())) {
            mostLike.add(args[0] + player.getName().substring(lookup.length()));
          }
        }
      }

    } else if (plugin.getConfiguration().isOfflineAutoComplete()) {
      for (CharSequence charSequence : plugin.getPlayerStorage().getAutoCompleteTree().getKeysStartingWith(args[0])) {
        mostLike.add(charSequence.toString());
      }
    } else {
      for (Player player : plugin.getServer().getOnlinePlayers()) {
        if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
          mostLike.add(player.getName());
        }
      }
    }

    if (mostLike.size() > 100) return mostLike.subList(0, 99);

    return mostLike;
  }
}
