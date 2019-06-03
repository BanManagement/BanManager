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

package me.confuser.banmanager.common.command.access;

import me.confuser.banmanager.common.config.ConfigKeys;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implements argument based permission checks for use in command implementations.
 */
public final class ArgumentPermissions {
    private ArgumentPermissions() {}

    private static final String USER_MODIFY_SELF = CommandPermission.ROOT + "modify.user.self";
    private static final String USER_MODIFY_OTHERS = CommandPermission.ROOT + "modify.user.others";
    private static final Function<String, String> GROUP_MODIFY = s -> CommandPermission.ROOT + "modify.group." + s;
    private static final Function<String, String> TRACK_MODIFY = s -> CommandPermission.ROOT + "modify.track." + s;

    private static final String USER_VIEW_SELF = CommandPermission.ROOT + "view.user.self";
    private static final String USER_VIEW_OTHERS = CommandPermission.ROOT + "view.user.others";
    private static final Function<String, String> GROUP_VIEW = s -> CommandPermission.ROOT + "view.group." + s;
    private static final Function<String, String> TRACK_VIEW = s -> CommandPermission.ROOT + "view.track." + s;

    private static final String CONTEXT_USE_GLOBAL = CommandPermission.ROOT + "usecontext.global";
    private static final BiFunction<String, String, String> CONTEXT_USE = (k, v) -> CommandPermission.ROOT + "usecontext." + k + "." + v;

    /**
     * Checks if the sender has permission to use the given arguments
     *
     * @param plugin the plugin instance
     * @param sender the sender to check
     * @param base the base permission for the command
     * @param args the arguments the sender is trying to use
     * @return true if the sender should NOT be allowed to use the arguments, true if they should
     */
    public static boolean checkArguments(BanManagerPlugin plugin, Sender sender, CommandPermission base, String... args) {
        if (!plugin.getConfiguration().get(ConfigKeys.USE_ARGUMENT_BASED_COMMAND_PERMISSIONS)) {
            return false;
        }

        if (args.length == 0) {
            throw new IllegalStateException();
        }

        StringBuilder permission = new StringBuilder(base.getPermission());
        for (String arg : args) {
            permission.append(".").append(arg);
        }

        return !sender.hasPermission(permission.toString());
    }


}
