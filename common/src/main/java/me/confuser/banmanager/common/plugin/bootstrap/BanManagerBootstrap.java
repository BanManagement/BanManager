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

package me.confuser.banmanager.common.plugin.bootstrap;

import me.confuser.banmanager.api.platform.PlatformType;
import me.confuser.banmanager.common.plugin.logging.PluginLogger;
import me.confuser.banmanager.common.plugin.scheduler.SchedulerAdapter;

import me.confuser.banmanager.common.sender.Sender;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

/**
 * Bootstrap plugin interface
 *
 * <p>Instances of this interface are responsible for loading the
 * "BanManager plugin" on their respective platforms.</p>
 */
public interface BanManagerBootstrap {

    /**
     * Gets the plugin logger
     *
     * @return the logger
     */
    PluginLogger getPluginLogger();

    /**
     * Gets an adapter for the platforms scheduler
     *
     * @return the scheduler
     */
    SchedulerAdapter getScheduler();

    /**
     * Returns a countdown latch which {@link CountDownLatch#countDown() counts down}
     * after the plugin has loaded.
     *
     * @return a loading latch
     */
    CountDownLatch getLoadLatch();

    /**
     * Returns a countdown latch which {@link CountDownLatch#countDown() counts down}
     * after the plugin has enabled.
     *
     * @return an enable latch
     */
    CountDownLatch getEnableLatch();

    /**
     * Gets a string of the plugin's version
     *
     * @return the version of the plugin
     */
    String getVersion();

    /**
     * Gets the time when the plugin first started in millis.
     *
     * @return the enable time
     */
    long getStartupTime();

    /**
     * Gets the platform type this instance of BanManager is running on.
     *
     * @return the platform type
     */
    PlatformType getType();

    /**
     * Gets the name or "brand" of the running platform
     *
     * @return the server brand
     */
    String getServerBrand();

    /**
     * Gets the version of the running platform
     *
     * @return the server version
     */
    String getServerVersion();

    /**
     * Gets the name associated with this server
     *
     * @return the server name
     */
    default @Nullable String getServerName() {
        return null;
    }

    /**
     * Gets the plugins main data storage directory
     *
     * <p>Bukkit: /root/plugins/LuckPerms</p>
     * <p>Bungee: /root/plugins/LuckPerms</p>
     * <p>Sponge: /root/luckperms/</p>
     *
     * @return the platforms data folder
     */
    Path getDataDirectory();

    /**
     * Gets the plugins configuration directory
     *
     * @return the config directory
     */
    default Path getConfigDirectory() {
        return getDataDirectory();
    }

    /**
     * Gets a bundled resource file from the jar
     *
     * @param path the path of the file
     * @return the file as an input stream
     */
    InputStream getResourceStream(String path);

    /**
     * Gets a player object linked to this User. The returned object must be the same type
     * as the instance used in the platforms ContextManager
     *
     * @param uuid the users unique id
     * @return a player object, or null, if one couldn't be found.
     */
    Optional<?> getPlayer(UUID uuid);

    /**
     * Gets a player object linked to this User. The returned object must be the same type
     * as the instance used in the platforms ContextManager
     *
     * @param username the username
     * @return a player object, or null, if one couldn't be found.
     */
    Optional<?> getPlayer(String username);

    /**
     * Gets a player object linked to this User. The returned object must be the same type
     * as the instance used in the platforms ContextManager
     *
     * @param username the username
     * @return a player object, or null, if one couldn't be found.
     */
    Optional<Sender> getPlayerAsSender(String username);

    /**
     * Gets a player object linked to this User. The returned object must be the same type
     * as the instance used in the platforms ContextManager
     *
     * @param uuid the users unique id
     * @return a player object, or null, if one couldn't be found.
     */
    Optional<Sender> getPlayerAsSender(UUID uuid);

    /**
     * Lookup a uuid from a username, using the servers internal uuid cache.
     *
     * @param username the username to lookup
     * @return an optional uuid, if found
     */
    Optional<UUID> lookupUuid(String username);

    /**
     * Lookup a username from a uuid, using the servers internal uuid cache.
     *
     * @param uuid the uuid to lookup
     * @return an optional username, if found
     */
    Optional<String> lookupUsername(UUID uuid);

    /**
     * Gets the number of users online on the platform
     *
     * @return the number of users
     */
    int getPlayerCount();

    /**
     * Gets the usernames of the users online on the platform
     *
     * @return a {@link List} of usernames
     */
    Stream<String> getPlayerList();

    /**
     * Gets the UUIDs of the users online on the platform
     *
     * @return a {@link Set} of UUIDs
     */
    Stream<UUID> getOnlinePlayers();

    /**
     * Checks if a user is online
     *
     * @param uuid the users external uuid
     * @return true if the user is online
     */
    boolean isPlayerOnline(UUID uuid);

    /**
     * Checks if a world exists
     *
     * @param name the uuid in string form, or name of the world
     * @return true if the world exists
     */
    boolean doesWorldExist(String name);



}