package me.confuser.banmanager.velocity;


import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.NonNull;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class VelocityCommand implements SimpleCommand {
  private CommonCommand command;
  private BMVelocityPlugin plugin;

  public VelocityCommand(CommonCommand command, BMVelocityPlugin plugin) {
    super();
    this.command = command;
    this.plugin = plugin;

    register();
  }

  @Override
  public void execute(final Invocation invocation) {
    CommandSource commandSource = invocation.source();
    String[] args = invocation.arguments();
    CommonSender commonSender = getSender(commandSource);
    boolean success = false;

    try {
      success = this.command.onCommand(commonSender, this.command.getParser(args));
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      e.printStackTrace();
    }

    if (!success) {
      commonSender.sendMessage(command.getUsage());
    }
  }

  private CommonSender getSender(CommandSource source) {
    if (source instanceof Player) {
      return new VelocityPlayer((Player) source, BanManagerPlugin.getInstance().getConfig().isOnlineMode());
    } else {
      return new VelocitySender(BanManagerPlugin.getInstance(), source);
    }
  }

  public void register() {
    String[] aliases = command.getAliases().toArray(new String[0]);
    CommandMeta meta = plugin.server.getCommandManager().metaBuilder(command.getCommandName())
            .aliases(Stream.of(aliases).toArray(String[]::new))
            .build();

    plugin.server.getCommandManager().register(meta, this);
  }

  @Override
  public List<String> suggest(@NonNull final Invocation invocation) {
    CommandSource commandSource = invocation.source();
    String[] args = invocation.arguments();
    if(!this.command.isEnableTabCompletion()) return Collections.emptyList();
    return this.command.handlePlayerNameTabComplete(getSender(commandSource), args);
  }
}
