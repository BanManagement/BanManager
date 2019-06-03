package me.confuser.banmanager.common.commands.global;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.CommandException;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.global.GlobalPlayerBanData;
import me.confuser.banmanager.util.CommandUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class BanAllCommand extends SingleCommand {

    public BanAllCommand(LocaleManager locale) {
        super(CommandSpec.BANALL.localize(locale), "banall", CommandPermission.BANALL, Predicates.alwaysFalse());
    }

    @Override
    public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) throws CommandException {

        if (args.size() < 2) {
            return CommandResult.INVALID_ARGS;
        }

        if (args.get(0).equalsIgnoreCase(sender.getName())) {
            Message.SENDER_ERROR_NOSELF.send(sender);
            return CommandResult.FAILURE;
        }

        // Check if UUID vs name
        final String playerName = args.get(0);
        final String reason = StringUtils.join(args, " ", 1, args.size());

        plugin.getBootstrap().getScheduler().executeAsync(() -> {
            final PlayerData player = CommandUtils.getPlayer(sender, playerName);

            if (player == null) {
                Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
                return;
            }

            final PlayerData actor = CommandUtils.getActor(sender);

            if (actor == null) return;

            final GlobalPlayerBanData ban = new GlobalPlayerBanData(player, actor, reason);
            int created;

            try {
                created = plugin.getGlobalPlayerBanStorage().create(ban);
            } catch (SQLException e) {
                Message.SENDER_ERROR_EXCEPTION.send(sender);
                e.printStackTrace();
                return;
            }

            if (created != 1) {
                return;
            }

            Message.BANALL_NOTIFY.send(sender,
                    "actor", ban.getActorName(),
                    "reason", ban.getReason(),
                    "player", player.getName(),
                    "playerId", player.getUUID().toString());
            });

        return CommandResult.SUCCESS;
    }

}
