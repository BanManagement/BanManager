package me.confuser.banmanager.fabric;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class FabricCommand {

  private CommonCommand command;

  public FabricCommand(CommonCommand command) {
    this.command = command;
  }

  public void register() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      registerCommand(dispatcher, command.getCommandName());

      for (String alias : command.getAliases()) {
        registerCommand(dispatcher, alias);
      }
    });
  }

  private void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, String name) {
    LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal(name)
      .requires(source -> Permissions.check(source, command.getPermission(), 4))
      .executes(this::execute)
      .then(CommandManager.argument("args", StringArgumentType.greedyString()).executes(this::execute));

    dispatcher.register(literal);
  }

  private int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    ServerCommandSource source = context.getSource();
    CommonSender commonSender = getSender(source);

    int start = context.getRange().getStart();
    String buffer = context.getInput().substring(start);
    List<String> args = new LinkedList<String>(Arrays.asList(buffer.split(" ")));
    args.remove(0);

    try {
      if (commonSender.hasPermission(command.getPermission())) {
        boolean success = this.command.onCommand(commonSender, this.command.getParser(args));

        if (!success) {
          commonSender.sendMessage(command.getUsage());
        }

        return success ? 1 : 0;
      } else {
        commonSender.sendMessage("&cYou do not have permission to use this command");
        return 1;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return 0;
  }

  private CommonSender getSender(ServerCommandSource source) {
    if (source.getEntity() instanceof ServerPlayerEntity) {
      return new FabricPlayer((ServerPlayerEntity) source.getEntity(), source.getServer(), BanManagerPlugin.getInstance().getConfig().isOnlineMode());
    } else {
      return new FabricSender(BanManagerPlugin.getInstance(), source);
    }
  }

  private CompletableFuture<Suggestions> suggest(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
    if (!this.command.isEnableTabCompletion()) {
      return builder.buildFuture();
    }

    ServerCommandSource source = context.getSource();
    CommonSender commonSender = getSender(source);

    String input = context.getInput().substring(context.getInput().indexOf(' ') + 1);
    String[] args = input.split(" ");

    List<String> suggestions = this.command.handlePlayerNameTabComplete(commonSender, args);

    for (String suggestion : suggestions) {
      builder.suggest(suggestion);
    }

    return builder.buildFuture();
  }
}
