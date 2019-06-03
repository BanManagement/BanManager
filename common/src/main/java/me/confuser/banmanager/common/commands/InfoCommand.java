package me.confuser.banmanager.common.commands;

import com.google.common.base.Predicates;
import com.google.common.net.InetAddresses;
import com.j256.ormlite.dao.CloseableIterator;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.CommandException;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.abstraction.SubCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.config.ConfigKeys;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.JSONCommandUtils;
import me.confuser.banmanager.util.parsers.InfoCommandParser;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InfoCommand extends SingleCommand {

    private static final FastDateFormat LAST_SEEN_COMMAND_FORMAT = FastDateFormat.getInstance("dd-MM-yyyy HH:mm:ss");

    public InfoCommand(LocaleManager locale) {
        super(CommandSpec.INFO.localize(locale), "bminfo", CommandPermission.BMINFO, Predicates.alwaysFalse());
    }

    @Override
    public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) throws CommandException {
        final InfoCommandParser parser;

        try {
            parser = new InfoCommandParser((String[]) argsIn.toArray());
        } catch (IllegalArgumentException e) {
            Message.INFO_ERROR_INCORRECTFLAGUSAGE.send(sender);
            return CommandResult.SUCCESS;
        }

        String[] args = parser.getArgs();

        if (args.length > 2) {
            return CommandResult.SUCCESS;
        }

        if (args.length == 0 && sender.isConsole()) {
            return CommandResult.SUCCESS;
        }

        if (args.length >= 1 && !sender.hasPermission("bm.command.bminfo.others")) {
            Message.SENDER_ERROR_NOPERMISSION.send(sender);
            return CommandResult.SUCCESS;
        }

        final String search = args.length > 0 ? args[0] : sender.getName();
        final boolean isName = !InetAddresses.isInetAddress(search);

        if (isName && search.length() > 16) {
            Message.SENDER_ERROR_INVALID_IP.send(sender);
            return CommandResult.SUCCESS;
        }

        final Integer index;

        try {
            index = args.length == 2 ? Integer.parseInt(args[1]) : null;
        } catch (NumberFormatException e) {
            Message.INFO_ERROR_INVALIDINDEX.send(sender);
            return CommandResult.SUCCESS;
        }

        plugin.getBootstrap().getScheduler().executeAsync(() -> {
            if (isName) {
                try {
                    playerInfo(plugin, sender, search, index, parser);
                } catch (SQLException e) {
                    Message.SENDER_ERROR_EXCEPTION.send(sender);
                    e.printStackTrace();
                    return;
                }
            }/* else {
                     TODO
                     ipInfo(sender, search);
                     }*/

        });


        return CommandResult.SUCCESS;
    }


    public void playerInfo(BanManagerPlugin plugin, Sender sender, String name, Integer index, InfoCommandParser parser) throws SQLException {
        List<PlayerData> players = plugin.getPlayerStorage().retrieve(name);

        if (players == null || players.size() == 0) {
            Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", name);
            return;
        }

        if (players.size() > 1 && (index == null || index > players.size() || index < 1)) {
            Message.INFO_ERROR_INDEXREQUIRED.send(sender, "size", players.size(), "name", name);

            int i = 0;
            for (PlayerData player : players) {
                i++;

                Message.INFO_ERROR_INDEX.send(sender, "index", i, "uuid", player.getUUID().toString(), "name", player.getName());
            }

            return;
        }

        if (players.size() == 1) index = 1;

        PlayerData player = players.get(index - 1);

        ArrayList<Object> messages = new ArrayList<>();

        boolean hasFlags = parser.isBans() || parser.isKicks() || parser.isMutes() || parser.isNotes() || parser
                .isWarnings() || parser.getIps() != null;

        if (hasFlags) {
            long since = 0;

            if (parser.getTime() != null && !parser.getTime().isEmpty()) {
                try {
                    since = DateUtils.parseDateDiff(parser.getTime(), false);
                } catch (Exception e1) {
                    Message.TIME_ERROR_INVALID.send(sender);
                    return;
                }
            }
            if (parser.isBans() && !sender.hasPermission("bm.command.bminfo.history.bans")) {
                Message.SENDER_ERROR_NOPERMISSION.send(sender);
                return;
            }

            if (parser.isKicks() && !sender.hasPermission("bm.command.bminfo.history.kicks")) {
                Message.SENDER_ERROR_NOPERMISSION.send(sender);
                return;
            }

            if (parser.isMutes() && !sender.hasPermission("bm.command.bminfo.history.mutes")) {
                Message.SENDER_ERROR_NOPERMISSION.send(sender);
                return;
            }

            if (parser.isNotes() && !sender.hasPermission("bm.command.bminfo.history.notes")) {
                Message.SENDER_ERROR_NOPERMISSION.send(sender);
                return;
            }

            if (parser.isWarnings() && !sender.hasPermission("bm.command.bminfo.history.warnings")) {
                Message.SENDER_ERROR_NOPERMISSION.send(sender);
                return;
            }

            if (parser.getIps() != null) {
                if (!sender.hasPermission("bm.command.bminfo.history.ips")) {
                    Message.SENDER_ERROR_NOPERMISSION.send(sender);
                    return;
                }

                int page = parser.getIps() - 1;

                if (page < 0) page = 0;

                handleIpHistory(plugin, messages, player, since, page);
            } else {

                ArrayList<HashMap<String, Object>> results;

                if (parser.getTime() != null && !parser.getTime().isEmpty()) {
                    results = plugin.getHistoryStorage().getSince(player, since, parser);
                } else {
                    results = plugin.getHistoryStorage().getAll(player, parser);
                }

                if (results == null || results.size() == 0) {
                    Message.INFO_HISTORY_NORESULTS.send(sender);
                    return;
                }

                String dateTimeFormat = Message.INFO_HISTORY_DATETIMEFORMAT.getMessage();
                FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

                for (HashMap<String, Object> result : results) {
                    messages.add(Message.INFO_HISTORY_ROW.asString(plugin.getLocaleManager(),
                            "id", result.get("id"),
                            "reason", result.get("reason"),
                            "type", result.get("type"),
                            "created", dateFormatter.format((long) result.get("created") * 1000L),
                            "actor", result.get("actor"),
                            "meta", result.get("meta")));
                }
            }

        } else {

            if (sender.hasPermission("bm.command.bminfo.playerstats")) {
                long banTotal = plugin.getPlayerBanRecordStorage().getCount(player);
                long muteTotal = plugin.getPlayerMuteRecordStorage().getCount(player);
                long warnTotal = plugin.getPlayerWarnStorage().getCount(player);
                double warnPointsTotal = plugin.getPlayerWarnStorage().getPointsCount(player);
                long kickTotal = plugin.getPlayerKickStorage().getCount(player);

                messages.add(Message.INFO_STATS_PLAYER.asString(plugin.getLocaleManager(),
                        "player", player.getName(),
                        "playerId", player.getUUID().toString(),
                        "bans", Long.toString(banTotal),
                        "mutes", Long.toString(muteTotal),
                        "warns", Long.toString(warnTotal),
                        "warnPoints", warnPointsTotal,
                        "kicks", Long.toString(kickTotal))
                );
            }

            if (sender.hasPermission("bm.command.bminfo.connection")) {
                messages.add(Message.INFO_CONNECTION.asString(plugin.getLocaleManager(),
                        IPUtils.toString(player.getIp()),
                        LAST_SEEN_COMMAND_FORMAT.format(player.getLastSeen() * 1000L)));
            }

            if (plugin.getConfiguration().get(ConfigKeys.GEOIP_ENABLED) && sender.hasPermission("bm.command.bminfo.geoip")) {
                Message message = Message.INFO_GEOIP;

                try {
                    InetAddress ip = IPUtils.toInetAddress(player.getIp());

                    CountryResponse countryResponse = plugin.getGeoIpConfig().getCountryDatabase().country(ip);
                    String country = countryResponse.getCountry().getName();
                    String countryIso = countryResponse.getCountry().getIsoCode();

                    CityResponse cityResponse = plugin.getGeoIpConfig().getCityDatabase().city(ip);
                    String city = cityResponse.getCity().getName();

                    messages.add(message.asString(plugin.getLocaleManager(), "country", country, "city", city, "countryIso", countryIso));

                } catch (IOException | GeoIp2Exception ignored) {
                }

            }

            if (sender.hasPermission("bm.command.bminfo.alts")) {
                messages.add(Message.ALTS_HEADER.getMessage());

                List<PlayerData> duplicatePlayers = plugin.getPlayerStorage().getDuplicates(player.getIp());

                if (!sender.isConsole()) {
                    messages.add(JSONCommandUtils.alts(duplicatePlayers));
                } else {
                    StringBuilder duplicates = new StringBuilder();

                    for (PlayerData duplicatePlayer : duplicatePlayers) {
                        duplicates.append(duplicatePlayer.getName()).append(", ");
                    }

                    if (duplicates.length() >= 2) duplicates.setLength(duplicates.length() - 2);

                    messages.add(duplicates.toString());
                }
            }

            if (sender.hasPermission("bm.command.bminfo.ipstats")) {

                long ipBanTotal = plugin.getIpBanRecordStorage().getCount(player.getIp());

                messages.add(Message.INFO_STATS_IP.asString(plugin.getLocaleManager(), Long.toString(ipBanTotal)));

                if (plugin.getIpBanStorage().isBanned(player.getIp())) {
                    IpBanData ban = plugin.getIpBanStorage().getBan(player.getIp());

                    Message message;

                    if (ban.getExpires() == 0) {
                        message = Message.INFO_IPBAN_PERMANENT;
                    } else {
                        message = Message.INFO_IPBAN_TEMPORARY;
                    }

                    String dateTimeFormat = Message.INFO_IPBAN_DATETIMEFORMAT.getMessage();

                    messages.add(
                            message.asString(sender.getPlugin().getLocaleManager(),
                                    "reason", ban.getReason(),
                                    "actor", ban.getActor().getName(),
                                    "created", FastDateFormat.getInstance(dateTimeFormat).format(ban.getCreated() * 1000L),
                                    "expires", DateUtils.getDifferenceFormat(ban.getExpires())
                            )
                    );
                }
            }

            if (plugin.getPlayerBanStorage().isBanned(player.getUUID())) {
                PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());

                Message message;

                if (ban.getExpires() == 0) {
                    message = Message.INFO_BAN_PERMANENT;
                } else {
                    message = Message.INFO_BAN_TEMPORARY;
                }

                String dateTimeFormat = Message.INFO_BAN_DATETIMEFORMAT.getMessage();

                messages.add(
                        message.asString(sender.getPlugin().getLocaleManager(),
                                "reason", ban.getReason(),
                                "actor", ban.getActor().getName(),
                                "created", FastDateFormat.getInstance(dateTimeFormat).format(ban.getCreated() * 1000L),
                                "expires", DateUtils.getDifferenceFormat(ban.getExpires())
                                )
                );
            }

            if (plugin.getPlayerMuteStorage().isMuted(player.getUUID())) {
                PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());

                Message message;

                if (mute.getExpires() == 0) {
                    message = Message.INFO_MUTE_PERMANENT;
                } else {
                    message = Message.INFO_MUTE_TEMPORARY;
                }

                String dateTimeFormat = Message.INFO_MUTE_DATETIMEFORMAT.getMessage();

                messages.add(
                        message.asString(sender.getPlugin().getLocaleManager(),
                                "reason", mute.getReason(),
                                "actor", mute.getActor().getName(),
                                "created", FastDateFormat.getInstance(dateTimeFormat).format(mute.getCreated() * 1000L),
                                "expires", DateUtils.getDifferenceFormat(mute.getExpires())
                        )
                );
            }

            if (sender.hasPermission("bm.command.bminfo.website")) {
                messages.add(
                        Message.INFO_WEBSITE_PLAYER.asString(sender.getPlugin().getLocaleManager(),
                                "player", player.getName(), "uuid", player.getUUID().toString(), "playerId", player.getUUID().toString()
                        )
                );
            }
        }

        // TODO Show last warning
        for (Object message : messages) {
            if (message instanceof String) {
                sender.sendMessage((String) message);
            } else if (message instanceof JSONMessage){
                ((JSONMessage) message).send((Player) sender);
            }
        }
    }

    private void handleIpHistory(BanManagerPlugin plugin, ArrayList<Object> messages, PlayerData player, long since, int page) {
        CloseableIterator<PlayerHistoryData> iterator = null;
        try {
            iterator = plugin.getPlayerHistoryStorage().getSince(player, since, page);

            String dateTimeFormat = Message.INFO_MUTE_DATETIMEFORMAT.getMessage();

            while (iterator.hasNext()) {
                PlayerHistoryData data = iterator.next();


                messages.add(
                        Message.INFO_IPS_ROW.asString(plugin.getLocaleManager(),
                                "ip", IPUtils.toString(data.getIp()),
                                "join", FastDateFormat.getInstance(dateTimeFormat).format(data.getJoin() * 1000L),
                                "leave", FastDateFormat.getInstance(dateTimeFormat).format(data.getLeave() * 1000L)
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (iterator != null) iterator.closeQuietly();
        }
    }

}
