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

package me.confuser.banmanager.common.config;

import me.confuser.banmanager.common.plugin.BanManagerPlugin;
//import me.lucko.luckperms.common.api.implementation.ApiConfiguration;

/**
 * The master configuration used by LuckPerms.
 */
public interface BanManagerConfiguration {

    /**
     * Gets the API delegate for this class.
     *
     * @return the api delegate
     */
    //ApiConfiguration getDelegate();

    /**
     * Gets the main plugin instance.
     *
     * @return the plugin instance
     */
    BanManagerPlugin getPlugin();

    /**
     * Gets the object which wraps the 'contexts.json' file.
     *
     * @return the contexts file wrapper object
     */
    //ContextsFile getContextsFile();

    /**
     * Reloads the configuration.
     */
    void reload();

    /**
     * Loads all configuration values.
     */
    void load();

    /**
     * Saves all configuration values.
     */
    void save();

    /**
     * Gets the value of a given context key.
     *
     * @param key the key
     * @param <T> the key return type
     * @return the value mapped to the given key. May be null.
     */
    <T> T get(ConfigKey<T> key);

    /**
     * Sets the value of a given context key.
     *
     * @param key the key
     * @param <T> the key return type
     */
    <T> void set(ConfigKeyTypes.FunctionalKey<T> key, T value);

}