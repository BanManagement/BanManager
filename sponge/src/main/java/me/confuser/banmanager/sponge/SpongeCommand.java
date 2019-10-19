package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpongeCommand implements CommandCallable {

  private BMSpongePlugin plugin;
  private CommonCommand command;

  public SpongeCommand(BMSpongePlugin plugin, CommonCommand command) {
    this.plugin = plugin;
    this.command = command;

    register();
  }

  public void register() {
    Sponge.getCommandManager().register(plugin, this, command.getCommandName());
  }

  @Override
  public CommandResult process(CommandSource source, String arguments) {
    CommonSender sender = getSender(source);
    boolean result = execute(sender, arguments);

    if (!result) {
      sender.sendMessage(command.getUsage());
      return CommandResult.empty();
    }

    return CommandResult.success();
  }

  private boolean execute(CommonSender sender, String arguments) {
    try {
      return this.command.onCommand(sender, this.command.getParser(arguments.split(" ")));
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition) {
    if (!command.isEnableTabCompletion()) return Collections.emptyList();

    return command.handlePlayerNameTabComplete(getSender(source), arguments.split(" "));
  }

  private CommonSender getSender(CommandSource source) {
    if (source instanceof Player) {
      return new SpongePlayer((Player) source, BanManagerPlugin.getInstance().getConfig().isOnlineMode());
    } else {
      return new SpongeSender(BanManagerPlugin.getInstance(), source);
    }
  }

  @Override
  public boolean testPermission(CommandSource source) {
    return source.hasPermission(command.getPermission());
  }

  @Override
  public Optional<Text> getShortDescription(CommandSource source) {
    return Optional.empty();
  }

  @Override
  public Optional<Text> getHelp(CommandSource source) {
    return Optional.empty();
  }

  @Override
  public Text getUsage(CommandSource source) {
    return Text.of(command.getUsage());
  }
}
