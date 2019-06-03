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

import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.util.TextUtils;
import net.kyori.text.Component;

import java.util.UUID;

public abstract class DummySender implements Sender {
    private final BanManagerPlugin platform;

    private final UUID uuid;
    private final String name;

    public DummySender(BanManagerPlugin plugin, UUID uuid, String name) {
        this.platform = plugin;
        this.uuid = uuid;
        this.name = name;
    }

    public DummySender(BanManagerPlugin plugin) {
        this(plugin, Sender.IMPORT_UUID, Sender.IMPORT_NAME);
    }

    protected abstract void consumeMessage(String s);

    @Override
    public void sendMessage(String message) {
        consumeMessage(message);
    }

    @Override
    public void sendMessage(Component message) {
        consumeMessage(TextUtils.toLegacy(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public BanManagerPlugin getPlugin() {
        return this.platform;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }
}