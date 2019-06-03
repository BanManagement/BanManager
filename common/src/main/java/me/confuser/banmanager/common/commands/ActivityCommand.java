package me.confuser.banmanager.common.commands;

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
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UUIDUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActivityCommand extends SingleCommand {

    public ActivityCommand(LocaleManager locale) {
        super(CommandSpec.ACTIVITY.localize(locale), "bmactivity", CommandPermission.BMACTIVITY, Predicates.alwaysFalse());
    }

    @Override
    public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) throws CommandException {

        if (args.size() == 0 || args.size() > 2) {
            return CommandResult.INVALID_ARGS;
        }

        long sinceCheck;

        try {
            sinceCheck = DateUtils.parseDateDiff(args.get(0), false);
        } catch (Exception e1) {
            Message.TIME_ERROR_INVALID.send(sender);
            return CommandResult.INVALID_ARGS;
        }

        final long since = sinceCheck;

        plugin.getBootstrap().getScheduler().executeAsync(() -> {
            List<Map<String, Object>> results;
            Message messageType = Message.BMACTIVITY_ROW_ALL;

            if (args.size() == 2) {
                messageType = Message.BMACTIVITY_ROW_PLAYER;

                PlayerData player = null;
                final boolean isUUID = args.get(1).length() > 16;

                if (isUUID) {
                    try {
                        player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(args.get(1))));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    player = plugin.getPlayerStorage().retrieve(args.get(1), false);
                }

                if (player == null) {
                    Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", args.get(1));
                    return;
                }

                results = plugin.getActivityStorage().getSince(since, player);
            } else {
                results = plugin.getActivityStorage().getSince(since);
            }

            if (results == null || results.size() == 0) {
                Message.BMACTIVITY_NO_RESULTS.send(sender);
                return;
            }

            String dateTimeFormat = Message.BMACTIVITY_ROW_DATE_TIME_FORMAT.getMessage();
            FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

            for (Map<String, Object> result : results) {
                messageType.send(sender,
                        "player", result.get("player"),
                        "type", result.get("type"),
                        "created", dateFormatter.format((long) result.get("created") * 1000L),
                        "actor", result.getOrDefault("actor", "")
                );
            }
        });

        return CommandResult.SUCCESS;
    }

}
