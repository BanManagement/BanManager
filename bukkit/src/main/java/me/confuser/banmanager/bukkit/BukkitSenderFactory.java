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

package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.bukkit.compat.CraftBukkitUtil;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.sender.SenderFactory;
import me.confuser.banmanager.common.util.Location;
import me.confuser.banmanager.util.TextUtils;
import net.kyori.text.Component;
import net.kyori.text.adapter.bukkit.TextAdapter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.UUID;

public class BukkitSenderFactory extends SenderFactory<CommandSender> {

    public BukkitSenderFactory(BanManagerPlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getName(CommandSender sender) {
        if (sender instanceof Player) {
            return sender.getName();
        }
        return Sender.CONSOLE_NAME;
    }

    @Override
    protected UUID getUuid(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId();
        }
        return Sender.CONSOLE_UUID;
    }

    @Override
    protected void sendMessage(CommandSender sender, String s) {
        // we can safely send async for players and the console
        if (sender instanceof Player || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
            sender.sendMessage(s);
            return;
        }

        // otherwise, send the message sync
        getPlugin().getBootstrap().getScheduler().executeSync(new SyncMessengerAgent(sender, s));
    }

    @Override
    protected void sendMessage(CommandSender sender, Component message) {
        if (CraftBukkitUtil.isChatCompatible() && sender instanceof Player) {
            TextAdapter.sendComponent(sender, message);
        } else {
            // Fallback to legacy format
            sendMessage(sender, TextUtils.toLegacy(message));
        }
    }

    @Override
    protected boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission(node);
    }

    @Override
    protected InetAddress getIPAddress(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return player.getAddress().getAddress();
        }
        return null;
    }

    @Override
    protected void kick(CommandSender sender, String message) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            player.kickPlayer(message);
        }
    }

    @Override
    protected Location getLocation(CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            org.bukkit.Location location = player.getLocation();
            return new Location(player.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }
        return null;
    }

    @Override
    protected void leaveVehicle(CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            player.leaveVehicle();
        }
    }

    @Override
    protected boolean isInsideVehicle(CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            return player.isInsideVehicle();
        }
        return false;
    }

    private static final class SyncMessengerAgent implements Runnable {
        private final CommandSender sender;
        private final String message;

        private SyncMessengerAgent(CommandSender sender, String message) {
            this.sender = sender;
            this.message = message;
        }

        @Override
        public void run() {
            this.sender.sendMessage(this.message);
        }
    }

}