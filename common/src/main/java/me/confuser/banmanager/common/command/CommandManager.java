/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.confuser.banmanager.common.command;

import com.google.common.collect.ImmutableList;
import me.confuser.banmanager.common.command.abstraction.Command;
import me.confuser.banmanager.common.command.abstraction.CommandException;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.command.tabcomplete.TabCompletions;
import me.confuser.banmanager.common.command.utils.ArgumentParser;
import me.confuser.banmanager.common.commands.*;
import me.confuser.banmanager.common.commands.global.*;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.util.TextUtils;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandManager {
    public static final Pattern COMMAND_SEPARATOR_PATTERN = Pattern.compile(" (?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");

    public static final char SECTION_CHAR = '\u00A7'; // §
    public static final char AMPERSAND_CHAR = '&';

    private final BanManagerPlugin plugin;

    // the default executor to run commands on
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final TabCompletions tabCompletions;

    private final List<Command<?, ?>> mainCommands;

    public CommandManager(BanManagerPlugin plugin) {
        this.plugin = plugin;
        LocaleManager locale = plugin.getLocaleManager();

        this.tabCompletions = new TabCompletions(plugin);

        this.mainCommands = ImmutableList.<Command<?, ?>>builder()
                .add(new InfoCommand(locale))
                .add(new ActivityCommand(locale))
                .add(new BanAllCommand(locale))
                .add(new BanIpAllCommand(locale))
                .add(new AddNoteAllCommand(locale))
                .add(new MuteAllCommand(locale))
                .add(new TempBanAllCommand(locale))
                .add(new TempBanIpAllCommand(locale))
                .add(new TempMuteAllCommand(locale))
                .add(new UnbanAllCommand(locale))
                .add(new UnbanIpAllCommand(locale))
                .add(new UnmuteAllCommand(locale))
                .add(new AddNoteCommand(locale))
                .add(new DeleteCommand(locale))
                .add(new BanCommand(locale))
                .add(new BanIpCommand(locale))
                .add(new BanIpRangeCommand(locale))
                .add(new BanListCommand(locale))
                .add(new WarnCommand(locale))
                .add(new UnmuteIpCommand(locale))
                .add(new UnmuteCommand(locale))
                .add(new RollbackCommand(locale))
                .add(new BanNameCommand(locale))
                .add(new ClearCommand(locale))
                .add(new UnbanNameCommand(locale))
                .add(new UnbanIpRangeCommand(locale))
                .add(new NotesCommand(locale))
                .add(new TempMuteCommand(locale))
                .add(new MuteCommand(locale))
                .add(new UnbanIpCommand(locale))
                .add(new DeleteLastWarningCommand(locale))
                .add(new ExportCommand(locale))
                .add(new ImportCommand(locale))
                .add(new FindAltsCommand(locale))
                .add(new KickCommand(locale))
                .add(new LoglessKickCommand(locale))
                .add(new UnbanCommand(locale))
                .add(new ReportCommand(locale))
                .add(new SyncCommand(locale))
                .add(new TempIpBanCommand(locale))
                .add(new TempBanCommand(locale))
                .add(new MuteIpCommand(locale))
                .add(new ReasonsCommand(locale))
                .add(new TempWarnCommand(locale))
                .add(new TempIpMuteCommand(locale))
                .add(new TempNameBanCommand(locale))
                .add(new TempIpRangeBanCommand(locale))
                .build();
    }

    public BanManagerPlugin getPlugin() {
        return this.plugin;
    }

    public TabCompletions getTabCompletions() {
        return this.tabCompletions;
    }

    public CompletableFuture<CommandResult> onCommand(Sender sender, String label, List<String> args) {
        return onCommand(sender, label, args, this.executor);
    }

    public CompletableFuture<CommandResult> onCommand(Sender sender, String label, List<String> args, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(sender, label, args);
            } catch (Throwable e) {
                this.plugin.getLogger().severe("Exception whilst executing command: " + args.toString());
                e.printStackTrace();
                return null;
            }
        }, executor);
    }

    public boolean hasPermissionForAny(Sender sender) {
        return this.mainCommands.stream().anyMatch(c -> c.shouldDisplay() && c.isAuthorized(sender));
    }

    @SuppressWarnings("unchecked")
    private CommandResult execute(Sender sender, String label, List<String> args) {
        List<String> arguments = new ArrayList<>(args);
        handleRewrites(arguments, true);

        // Handle no arguments
        if (arguments.isEmpty() || (arguments.size() == 1 && arguments.get(0).trim().isEmpty())) {
            Message.BLANK.send(sender, "&2Running &BanManager v" + this.plugin.getBootstrap().getVersion() + "&2.");
            if (hasPermissionForAny(sender)) {
                Message.VIEW_AVAILABLE_COMMANDS_PROMPT.send(sender, label);
                return CommandResult.SUCCESS;
            } else {
                Collection<? extends Group> groups = this.plugin.getGroupManager().getAll().values();
                if (groups.size() <= 1 && groups.stream().allMatch(g -> g.getOwnNodes().isEmpty())) {
                    Message.FIRST_TIME_SETUP.send(sender, label, sender.getName());
                } else {
                    Message.NO_PERMISSION_FOR_SUBCOMMANDS.send(sender);
                }
                return CommandResult.NO_PERMISSION;
            }
        }

        // Look for the main command.
        Optional<Command<?, ?>> o = this.mainCommands.stream()
                .filter(m -> m.getName().equalsIgnoreCase(arguments.get(0)))
                .limit(1)
                .findAny();

        // Main command not found
        if (!o.isPresent()) {
            sendCommandUsage(sender, label);
            return CommandResult.INVALID_ARGS;
        }

        // Check the Sender has permission to use the main command.
        final Command main = o.get();
        if (!main.isAuthorized(sender)) {
            sendCommandUsage(sender, label);
            return CommandResult.NO_PERMISSION;
        }

        arguments.remove(0); // remove the main command arg.

        // Check the correct number of args were given for the main command
        if (main.getArgumentCheck().test(arguments.size())) {
            main.sendDetailedUsage(sender, label);
            return CommandResult.INVALID_ARGS;
        }

        // Try to execute the command.
        CommandResult result;
        try {
            result = main.execute(this.plugin, sender, null, arguments, label);
        } catch (CommandException e) {
            result = handleException(e, sender, label, main);
        } catch (Throwable e) {
            e.printStackTrace();
            result = CommandResult.FAILURE;
        }

        return result;
    }

    /**
     * Generic tab complete method to be called from the command executor object of the platform
     *
     * @param sender who is tab completing
     * @param args   the arguments provided so far
     * @return a list of suggestions
     */
    @SuppressWarnings("unchecked")
    public List<String> onTabComplete(Sender sender, List<String> args) {
        List<String> arguments = new ArrayList<>(args);

        // we rewrite tab completions too!
        handleRewrites(arguments, false);

        final List<Command> mains = this.mainCommands.stream()
                .filter(Command::shouldDisplay)
                .filter(m -> m.isAuthorized(sender))
                .collect(Collectors.toList());

        // Not yet past the point of entering a main command
        if (arguments.size() <= 1) {

            // Nothing yet entered
            if (arguments.isEmpty() || arguments.get(0).equals("")) {
                return mains.stream()
                        .map(m -> m.getName().toLowerCase())
                        .collect(Collectors.toList());
            }

            // Started typing a main command
            return mains.stream()
                    .map(m -> m.getName().toLowerCase())
                    .filter(s -> s.startsWith(arguments.get(0).toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Find a main command matching the first arg
        Optional<Command> o = mains.stream()
                .filter(m -> m.getName().equalsIgnoreCase(arguments.get(0)))
                .findFirst();

        arguments.remove(0); // remove the main command arg.

        // Pass the processing onto the main command
        return o.map(cmd -> cmd.tabComplete(this.plugin, sender, arguments)).orElseGet(Collections::emptyList);
    }

    private void sendCommandUsage(Sender sender, String label) {
        Message.BLANK.send(sender, "&2Running &bLuckPerms v" + this.plugin.getBootstrap().getVersion() + "&2.");
        this.mainCommands.stream()
                .filter(Command::shouldDisplay)
                .filter(c -> c.isAuthorized(sender))
                .forEach(c -> {
                    String permission = c.getPermission().map(CommandPermission::getPermission).orElse("None");

                    TextComponent component = TextUtils.fromLegacy("&3> &a" + String.format(c.getUsage(), label), AMPERSAND_CHAR)
                            .toBuilder().applyDeep(comp -> {
                                comp.hoverEvent(HoverEvent.showText(TextUtils.fromLegacy(TextUtils.joinNewline(
                                        "&bCommand: &2" + c.getName(),
                                        "&bDescription: &2" + c.getDescription(),
                                        "&bUsage: &2" + String.format(c.getUsage(), label),
                                        "&bPermission: &2" + permission,
                                        " ",
                                        "&7Click to auto-complete."
                                ), AMPERSAND_CHAR)));
                                comp.clickEvent(ClickEvent.suggestCommand(String.format(c.getUsage(), label)));
                            }).build();
                    sender.sendMessage(component);
                });
    }

    public static CommandResult handleException(CommandException e, Sender sender, String label, Command command) {
        if (e instanceof ArgumentParser.ArgumentException) {
            if (e instanceof ArgumentParser.DetailedUsageException) {
                command.sendDetailedUsage(sender, label);
                return CommandResult.INVALID_ARGS;
            }

            if (e instanceof ArgumentParser.InvalidServerWorldException) {
                Message.SERVER_WORLD_INVALID_ENTRY.send(sender);
                return CommandResult.INVALID_ARGS;
            }

            if (e instanceof ArgumentParser.PastDateException) {
                Message.PAST_DATE_ERROR.send(sender);
                return CommandResult.INVALID_ARGS;
            }

            if (e instanceof ArgumentParser.InvalidDateException) {
                Message.ILLEGAL_DATE_ERROR.send(sender, ((ArgumentParser.InvalidDateException) e).getInvalidDate());
                return CommandResult.INVALID_ARGS;
            }

            if (e instanceof ArgumentParser.InvalidPriorityException) {
                Message.META_INVALID_PRIORITY.send(sender, ((ArgumentParser.InvalidPriorityException) e).getInvalidPriority());
                return CommandResult.INVALID_ARGS;
            }
        }

        // Not something we can catch.
        e.printStackTrace();
        return CommandResult.FAILURE;
    }

    /**
     * Handles aliases
     *
     * @param args the current args list
     * @param rewriteLastArgument if the last argument should be rewritten - this is false when the method is called on tab completions
     */
    private static void handleRewrites(List<String> args, boolean rewriteLastArgument) {
        // Provide aliases
        if (args.size() >= 1 && (rewriteLastArgument || args.size() >= 2)) {
            String arg0 = args.get(0);
            if (arg0.equalsIgnoreCase("u") || arg0.equalsIgnoreCase("player") || arg0.equalsIgnoreCase("p")) {
                args.remove(0);
                args.add(0, "user");
            } else if (arg0.equalsIgnoreCase("g")) {
                args.remove(0);
                args.add(0, "group");
            } else if (arg0.equalsIgnoreCase("t")) {
                args.remove(0);
                args.add(0, "track");
            } else if (arg0.equalsIgnoreCase("i")) {
                args.remove(0);
                args.add(0, "info");
            }
        }

        if (args.size() >= 3 && (rewriteLastArgument || args.size() >= 4)) {
            if (!args.get(0).equalsIgnoreCase("user") && !args.get(0).equalsIgnoreCase("group")) {
                return;
            }

            String s = args.get(2).toLowerCase();
            switch (s) {
                // Provide aliases
                case "p":
                case "perm":
                case "perms":
                    args.remove(2);
                    args.add(2, "permission");
                    break;
                case "chat":
                case "m":
                    args.remove(2);
                    args.add(2, "meta");
                    break;
                case "i":
                case "about":
                case "list":
                    args.remove(2);
                    args.add(2, "info");
                    break;
                case "inherit":
                case "inheritances":
                case "group":
                case "groups":
                case "g":
                case "rank":
                case "ranks":
                case "parents":
                    args.remove(2);
                    args.add(2, "parent");
                    break;
                case "e":
                    args.remove(2);
                    args.add(2, "editor");
                    break;

                // Provide backwards compatibility
                case "setprimarygroup":
                case "switchprimarygroup":
                    args.remove(2);
                    args.add(2, "parent");
                    args.add(3, "switchprimarygroup");
                    break;
                case "listnodes":
                    args.remove(2);
                    args.add(2, "permission");
                    args.add(3, "info");
                    break;
                case "set":
                case "unset":
                case "settemp":
                case "unsettemp":
                    args.add(2, "permission");
                    break;
                case "haspermission":
                    args.remove(2);
                    args.add(2, "permission");
                    args.add(3, "check");
                    break;
                case "inheritspermission":
                    args.remove(2);
                    args.add(2, "permission");
                    args.add(3, "checkinherits");
                    break;
                case "listgroups":
                    args.remove(2);
                    args.add(2, "parent");
                    args.add(3, "info");
                    break;
                case "addgroup":
                case "setinherit":
                    args.remove(2);
                    args.add(2, "parent");
                    args.add(3, "add");
                    break;
                case "setgroup":
                    args.remove(2);
                    args.add(2, "parent");
                    args.add(3, "set");
                    break;
                case "removegroup":
                case "unsetinherit":
                    args.remove(2);
                    args.add(2, "parent");
                    args.add(3, "remove");
                    break;
                case "addtempgroup":
                case "settempinherit":
                    args.remove(2);
                    args.add(2, "parent");
                    args.add(3, "addtemp");
                    break;
                case "removetempgroup":
                case "unsettempinherit":
                    args.remove(2);
                    args.add(2, "parent");
                    args.add(3, "removetemp");
                    break;
                case "chatmeta":
                    args.remove(2);
                    args.add(2, "meta");
                    args.add(3, "info");
                    break;
                case "addprefix":
                case "addsuffix":
                case "removeprefix":
                case "removesuffix":
                case "addtempprefix":
                case "addtempsuffix":
                case "removetempprefix":
                case "removetempsuffix":
                    args.add(2, "meta");
                    break;
                default:
                    break;
            }

            // provide lazy info
            boolean lazyInfo = (
                    args.size() >= 4 && (rewriteLastArgument || args.size() >= 5) &&
                    (args.get(2).equalsIgnoreCase("permission") || args.get(2).equalsIgnoreCase("parent") || args.get(2).equalsIgnoreCase("meta")) &&
                    (args.get(3).equalsIgnoreCase("i") || args.get(3).equalsIgnoreCase("about") || args.get(3).equalsIgnoreCase("list"))
            );

            if (lazyInfo) {
                args.remove(3);
                args.add(3, "info");
            }

            // Provide lazy set rewrite
            boolean lazySet = (
                    args.size() >= 6 && (rewriteLastArgument || args.size() >= 7) &&
                    args.get(2).equalsIgnoreCase("permission") &&
                    args.get(3).toLowerCase().startsWith("set") &&
                    (args.get(5).equalsIgnoreCase("none") || args.get(5).equalsIgnoreCase("0"))
            );

            if (lazySet) {
                args.remove(5);
                args.remove(3);
                args.add(3, "unset");
            }
        }
    }

    /**
     * Strips outer quote marks from a list of parsed arguments.
     *
     * @param input the list of arguments to strip quotes from
     * @return an ArrayList containing the contents of input without quotes
     */
    public static List<String> stripQuotes(List<String> input) {
        input = new ArrayList<>(input);
        ListIterator<String> iterator = input.listIterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (value.length() < 3) {
                continue;
            }

            if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                iterator.set(value.substring(1, value.length() - 1));
            }
        }
        return input;
    }

}
