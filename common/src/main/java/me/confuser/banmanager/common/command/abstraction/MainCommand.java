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


import com.google.common.base.Predicates;
import me.confuser.banmanager.common.command.CommandManager;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.locale.command.LocalizedCommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class MainCommand<T, I> extends Command<Void, T> {

    // equals 1 if the command doesn't take a mid argument, e.g. /lp log sub-command....
    // equals 2 if the command does take a mid argument, e.g. /lp user <USER> sub-command....
    private final int minArgs;

    public MainCommand(LocalizedCommandSpec spec, String name, int minArgs, List<Command<T, ?>> children) {
        super(spec, name, null, Predicates.alwaysFalse(), children);
        this.minArgs = minArgs;
    }

    @Override
    public CommandResult execute(BanManagerPlugin plugin, Sender sender, Void v, List<String> args, String label) {
        if (args.size() < this.minArgs) {
            sendUsage(sender, label);
            return CommandResult.INVALID_ARGS;
        }

        Optional<Command<T, ?>> o = getChildren().get().stream()
                .filter(s -> s.getName().equalsIgnoreCase(args.get(this.minArgs - 1)))
                .limit(1)
                .findAny();

        if (!o.isPresent()) {
            Message.COMMAND_NOT_RECOGNISED.send(sender);
            return CommandResult.INVALID_ARGS;
        }

        final Command<T, ?> sub = o.get();
        if (!sub.isAuthorized(sender)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return CommandResult.NO_PERMISSION;
        }

        List<String> strippedArgs = new ArrayList<>();
        if (args.size() > this.minArgs) {
            strippedArgs.addAll(args.subList(this.minArgs, args.size()));
        }

        if (sub.getArgumentCheck().test(strippedArgs.size())) {
            sub.sendDetailedUsage(sender, label);
            return CommandResult.INVALID_ARGS;
        }

        final String name = args.get(0);
        I targetId = parseTarget(name, plugin, sender);
        if (targetId == null) {
            return CommandResult.LOADING_ERROR;
        }

        ReentrantLock lock = getLockForTarget(targetId);
        lock.lock();
        try {
            T target = getTarget(targetId, plugin, sender);
            if (target != null) {
                CommandResult result;
                try {
                    result = sub.execute(plugin, sender, target, strippedArgs, label);
                } catch (CommandException e) {
                    result = CommandManager.handleException(e, sender, label, sub);
                }

                cleanup(target, plugin);
                return result;
            }
        } finally {
            lock.unlock();
        }

        return CommandResult.LOADING_ERROR;
    }

    @Override
    public List<String> tabComplete(BanManagerPlugin plugin, Sender sender, List<String> args) {
        final List<String> objects = getTargets(plugin);

        if (args.size() <= 1) {
            if (args.isEmpty() || args.get(0).trim().isEmpty()) {
                return objects;
            }

            return objects.stream()
                    .filter(s -> s.toLowerCase().startsWith(args.get(0).toLowerCase()))
                    .collect(Collectors.toList());
        }

        final List<Command<T, ?>> subs = getChildren().get().stream()
                .filter(s -> s.isAuthorized(sender))
                .collect(Collectors.toList());

        if (args.size() == 2) {
            if (args.get(1).trim().isEmpty()) {
                return subs.stream()
                        .map(m -> m.getName().toLowerCase())
                        .collect(Collectors.toList());
            }

            return subs.stream()
                    .map(m -> m.getName().toLowerCase())
                    .filter(s -> s.toLowerCase().startsWith(args.get(1).toLowerCase()))
                    .collect(Collectors.toList());
        }

        Optional<Command<T, ?>> o = subs.stream()
                .filter(s -> s.getName().equalsIgnoreCase(args.get(1)))
                .findFirst();

        return o.map(cmd -> cmd.tabComplete(plugin, sender, args.subList(2, args.size()))).orElseGet(Collections::emptyList);

    }

    protected abstract List<String> getTargets(BanManagerPlugin plugin);

    protected abstract I parseTarget(String target, BanManagerPlugin plugin, Sender sender);

    protected abstract ReentrantLock getLockForTarget(I target);

    protected abstract T getTarget(I target, BanManagerPlugin plugin, Sender sender);

    protected abstract void cleanup(T t, BanManagerPlugin plugin);

    @Override
    public void sendUsage(Sender sender, String label) {
        List<Command> subs = getChildren().get().stream()
                .filter(s -> s.isAuthorized(sender))
                .collect(Collectors.toList());

        if (!subs.isEmpty()) {
            Message.MAIN_COMMAND_USAGE_HEADER.send(sender, getName(), String.format(getUsage(), label));
            for (Command s : subs) {
                s.sendUsage(sender, label);
            }
        } else {
            Message.COMMAND_NO_PERMISSION.send(sender);
        }
    }

    @Override
    public void sendDetailedUsage(Sender sender, String label) {
        sendUsage(sender, label);
    }

    @Override
    public boolean isAuthorized(Sender sender) {
        return getChildren().get().stream().anyMatch(sc -> sc.isAuthorized(sender));
    }

    @Override
    public @NonNull Optional<List<Command<T, ?>>> getChildren() {
        return super.getChildren();
    }
}
