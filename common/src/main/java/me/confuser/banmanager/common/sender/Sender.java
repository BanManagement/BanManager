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

package me.confuser.banmanager.common.sender;

import me.confuser.banmanager.common.command.CommandManager;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.util.Location;
import net.kyori.text.Component;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Wrapper interface to represent a CommandSender/CommandSource within the common command implementations.
 */
public interface Sender {

    /** The uuid used by the console sender. */
    UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    /** The name used by the console sender. */
    String CONSOLE_NAME = "Console";
    /** The uuid used by the 'import' sender. */
    UUID IMPORT_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    /** The name used by the 'import' sender. */
    String IMPORT_NAME = "Import";

    /**
     * Gets the plugin instance the sender is from.
     *
     * @return the plugin
     */
    BanManagerPlugin getPlugin();

    /**
     * Gets the sender's username
     *
     * @return a friendly username for the sender
     */
    String getName();

    /**
     * Gets the sender's display name
     *
     * @return a the display name for the sender
     */
    String getDisplayName();

    /**
     * Gets the sender's unique id.
     *
     * <p>See {@link #CONSOLE_UUID} for the console's UUID representation.</p>
     *
     * @return the sender's uuid
     */
    UUID getUuid();

    /**
     * Send a message to the Sender.
     *
     * <p>Supports {@link CommandManager#SECTION_CHAR} for message formatting.</p>
     *
     * @param message the message to send.
     */
    void sendMessage(String message);

    /**
     * Send a json message to the Sender.
     *
     * @param message the message to send.
     */
    void sendMessage(Component message);

    /**
     * Check if the Sender has a permission.
     *
     * @param permission the permission to check for
     * @return true if the sender has the permission
     */
    boolean hasPermission(String permission);

    /**
     * Check if the Sender has a permission.
     *
     * @param permission the permission to check for
     * @return true if the sender has the permission
     */
    default boolean hasPermission(CommandPermission permission) {
        return hasPermission(permission.getPermission());
    }

    /**
     * Gets whether this sender is the console
     *
     * @return if the sender is the console
     */
    default boolean isConsole() {
        return CONSOLE_UUID.equals(getUuid()) || IMPORT_UUID.equals(getUuid());
    }

    /**
     * Gets whether this sender is an import process
     *
     * @return if the sender is an import process
     */
    default boolean isImport() {
        return IMPORT_UUID.equals(getUuid());
    }

    /**
     * Gets whether this sender is still valid & receiving messages.
     *
     * @return if this sender is valid
     */
    default boolean isValid() {
        return true;
    }

    /**
     * Gets the senders IP address
     *
     * @return Ip address
     */
    InetAddress getIPAddress();

    /**
     * Kicks the sender from the server with the specified message
     *
     * @param message the message to show the kicked player
     */
    void kick(String message);

    /**
     * Teleports the sender to the specified location
     *
     * @param location the location to teleport the sender to
     */
    void teleport(Location location);

    boolean isInsideVehicle();
    void leaveVehicle();

    Location getLocation();

}