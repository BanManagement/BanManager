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

package me.confuser.banmanager.common.command.abstraction;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.command.Argument;
import me.confuser.banmanager.common.locale.command.LocalizedCommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;

import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a single "main" command (one without any children)
 */
public abstract class SingleCommand extends Command<Void, Void> {

    public SingleCommand(LocalizedCommandSpec spec, String name, CommandPermission permission, Predicate<Integer> argumentCheck) {
        super(spec, name, permission, argumentCheck, null);
    }

    @Override
    public CommandResult execute(BanManagerPlugin plugin, Sender sender, Void v, List<String> args, String label) throws CommandException {
        return execute(plugin, sender, args, label);
    }

    public abstract CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) throws CommandException;

    @Override
    public void sendUsage(Sender sender, String label) {
        StringBuilder sb = new StringBuilder();
        if (getArgs().isPresent()) {
            sb.append(Message.COMMAND_USAGE_ARGUMENT_JOIN.asString(sender.getPlugin().getLocaleManager()));
            for (Argument arg : getArgs().get()) {
                sb.append(arg.asPrettyString(sender.getPlugin().getLocaleManager())).append(" ");
            }
        }

        Message.COMMAND_USAGE_BRIEF.send(sender, getName().toLowerCase(), sb.toString());
    }

    @Override
    public void sendDetailedUsage(Sender sender, String label) {
        Message.COMMAND_USAGE_DETAILED_HEADER.send(sender, getName(), getDescription());

        if (getArgs().isPresent()) {
            Message.COMMAND_USAGE_DETAILED_ARGS_HEADER.send(sender);
            for (Argument arg : getArgs().get()) {
                Message.COMMAND_USAGE_DETAILED_ARG.send(sender, arg.asPrettyString(sender.getPlugin().getLocaleManager()), arg.getDescription());
            }
        }
    }
}
