package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SpongeCommand {

    private final BanManagerPlugin plugin;
    private final CommonCommand command;
    private final PluginContainer pluginContainer;

    public SpongeCommand(BanManagerPlugin plugin, CommonCommand command, PluginContainer pluginContainer) {
        this.plugin = plugin;
        this.command = command;
        this.pluginContainer = pluginContainer;
    }

    public void register() {
        Command.Parameterized cmd = buildCommand();
        Sponge.server().commandManager().registrar(Command.Parameterized.class)
            .orElseThrow(() -> new IllegalStateException("Command registrar not found"))
            .register(pluginContainer, cmd, command.getCommandName(), command.getAliases().toArray(new String[0]));
    }

    public Command.Parameterized buildCommand() {
        Parameter.Value<String> argsParam = Parameter.remainingJoinedStrings()
            .key("args")
            .optional()
            .build();

        return Command.builder()
            .addParameter(argsParam)
            .permission(command.getPermission())
            .executor(context -> execute(context, argsParam))
            .build();
    }

    private CommandResult execute(CommandContext context, Parameter.Value<String> argsParam) throws CommandException {
        CommandCause cause = context.cause();
        CommonSender sender = getSender(cause);

        Optional<String> argsOpt = context.one(argsParam);
        List<String> args = new LinkedList<>();

        if (argsOpt.isPresent()) {
            String argsStr = argsOpt.get();
            if (!argsStr.isEmpty()) {
                args.addAll(Arrays.asList(argsStr.split(" ")));
            }
        }

        try {
            if (sender.hasPermission(command.getPermission())) {
                boolean success = this.command.onCommand(sender, this.command.getParser(args));

                if (!success) {
                    sender.sendMessage(command.getUsage());
                }

                return CommandResult.success();
            } else {
                sender.sendMessage("&cYou do not have permission to use this command");
                return CommandResult.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CommandResult.error(Component.text("An error occurred: " + e.getMessage()));
        }
    }

    private CommonSender getSender(CommandCause cause) {
        Object root = cause.root();
        if (root instanceof ServerPlayer) {
            return new SpongePlayer((ServerPlayer) root, plugin.getConfig().isOnlineMode());
        } else {
            // Use CommandCause directly as the audience - this properly routes
            // messages back to RCON connections and other command sources
            return new SpongeSender(plugin, cause);
        }
    }
}
