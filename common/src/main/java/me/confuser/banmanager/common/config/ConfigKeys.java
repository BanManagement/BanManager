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

//TODO redo for banmanager
package me.confuser.banmanager.common.config;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static me.confuser.banmanager.common.config.ConfigKeyTypes.*;


/**
 * All of the {@link ConfigKey}s used by BanManager.
 *
 * <p>The {@link #getKeys()} method and associated behaviour allows this class
 * to function a bit like an enum, but with generics.</p>
 */
public final class ConfigKeys {
    private ConfigKeys() {}

    /**
     * The name of the server
     */
    public static final ConfigKey<String> SERVER = lowercaseStringKey("server", "global");

    /**
     * How many minutes to wait between syncs. A value <= 0 will disable syncing.
     */
    public static final ConfigKey<Integer> SYNC_TIME = enduringKey(customKey(c -> {
        int val = c.getInteger("sync-minutes", -1);
        if (val == -1) {
            val = c.getInteger("data.sync-minutes", -1);
        }
        return val;
    }));



    //BanManager

    //Database
    public static final ConfigKey<Boolean> DATABASE_ENABLED = booleanKey("database.enabled", true);
    public static final ConfigKey<String> DATABASE_STORAGETYPE = stringKey("database.storageType", "mysql");
    public static final ConfigKey<String> DATABASE_HOST = stringKey("database.host", "");
    public static final ConfigKey<Integer> DATABASE_PORT = integerKey("database.port", 3306);
    public static final ConfigKey<String> DATABASE_NAME = stringKey("database.name", "");
    public static final ConfigKey<String> DATABASE_USER = stringKey("database.user", "");
    public static final ConfigKey<String> DATABASE_PASSWORD = stringKey("database.password", "");
    public static final ConfigKey<Boolean> DATABASE_USE_SSL = booleanKey("database.useSSL", false);
    public static final ConfigKey<Boolean> DATABASE_VERIFY_SERVER_CERT = booleanKey("database.verifyServerCertificate", false);
    public static final ConfigKey<Integer> DATABASE_MAX_CONNECTIONS = integerKey("database.maxConnections", 10);
    public static final ConfigKey<Integer> DATABASE_LEAK_DETECTION = integerKey("database.leakDetection", 0);
    public static final ConfigKey<Map<String, String>> DATABASE_TABLES = mapKey("database.tables");

    //Console
    public static final ConfigKey<String> CONSOLE_NAME = stringKey("console.name", "Console");
    public static final ConfigKey<UUID> CONSOLE_UUID = uuidKey("console.uuid", UUID.randomUUID());

    //Geoip
    public static final ConfigKey<Boolean> GEOIP_ENABLED = booleanKey("geoip.enabled", false);

    //End of BanManager




    /**
     * # If the servers own UUID cache/lookup facility should be used when there is no record for a player in the LuckPerms cache.
     */
    public static final ConfigKey<Boolean> USE_SERVER_UUID_CACHE = booleanKey("use-server-uuid-cache", false);

    /**
     * If LuckPerms should allow usernames with non alphanumeric characters.
     */
    public static final ConfigKey<Boolean> ALLOW_INVALID_USERNAMES = booleanKey("allow-invalid-usernames", false);

    /**
     * If LuckPerms should produce extra logging output when it handles logins.
     */
    public static final ConfigKey<Boolean> DEBUG_LOGINS = booleanKey("debug-logins", false);

    /**
     * If LP should cancel login attempts for players whose permission data could not be loaded.
     */
    public static final ConfigKey<Boolean> CANCEL_FAILED_LOGINS = booleanKey("cancel-failed-logins", false);

    /**
     * How primary groups should be calculated.
     */
    public static final ConfigKey<String> PRIMARY_GROUP_CALCULATION_METHOD = enduringKey(customKey(c -> {
        String option = c.getString("primary-group-calculation", "stored").toLowerCase();
        if (!option.equals("stored") && !option.equals("parents-by-weight") && !option.equals("all-parents-by-weight")) {
            option = "stored";
        }

        return option;
    }));

    /**
     * If set to false, the plugin will allow a Users primary group to be removed with the
     * 'parent remove' command, and will set their primary group back to default.
     */
    public static final ConfigKey<Boolean> PREVENT_PRIMARY_GROUP_REMOVAL = booleanKey("prevent-primary-group-removal", true);

    /**
     * If the plugin should check for "extra" permissions with users run LP commands
     */
    public static final ConfigKey<Boolean> USE_ARGUMENT_BASED_COMMAND_PERMISSIONS = booleanKey("argument-based-command-permissions", false);

    /**
     * If the plugin should check whether senders are a member of a given group
     * before they're able to edit the groups permissions or add/remove it from other users.
     */
    public static final ConfigKey<Boolean> REQUIRE_SENDER_GROUP_MEMBERSHIP_TO_MODIFY = booleanKey("require-sender-group-membership-to-modify", false);

    /**
     * If wildcards are being applied
     */
    public static final ConfigKey<Boolean> APPLYING_WILDCARDS = enduringKey(booleanKey("apply-wildcards", true));

    /**
     * If regex permissions are being applied
     */
    public static final ConfigKey<Boolean> APPLYING_REGEX = enduringKey(booleanKey("apply-regex", true));

    /**
     * If shorthand permissions are being applied
     */
    public static final ConfigKey<Boolean> APPLYING_SHORTHAND = enduringKey(booleanKey("apply-shorthand", true));

    /**
     * If Bukkit child permissions are being applied. This setting is ignored on other platforms.
     */
    public static final ConfigKey<Boolean> APPLY_BUKKIT_CHILD_PERMISSIONS = enduringKey(booleanKey("apply-bukkit-child-permissions", true));

    /**
     * If Bukkit default permissions are being applied. This setting is ignored on other platforms.
     */
    public static final ConfigKey<Boolean> APPLY_BUKKIT_DEFAULT_PERMISSIONS = enduringKey(booleanKey("apply-bukkit-default-permissions", true));

    /**
     * If Bukkit attachment permissions are being applied. This setting is ignored on other platforms.
     */
    public static final ConfigKey<Boolean> APPLY_BUKKIT_ATTACHMENT_PERMISSIONS = enduringKey(booleanKey("apply-bukkit-attachment-permissions", true));

    /**
     * If Nukkit child permissions are being applied. This setting is ignored on other platforms.
     */
    public static final ConfigKey<Boolean> APPLY_NUKKIT_CHILD_PERMISSIONS = enduringKey(booleanKey("apply-nukkit-child-permissions", true));

    /**
     * If Nukkit default permissions are being applied. This setting is ignored on other platforms.
     */
    public static final ConfigKey<Boolean> APPLY_NUKKIT_DEFAULT_PERMISSIONS = enduringKey(booleanKey("apply-nukkit-default-permissions", true));

    /**
     * If Nukkit attachment permissions are being applied. This setting is ignored on other platforms.
     */
    public static final ConfigKey<Boolean> APPLY_NUKKIT_ATTACHMENT_PERMISSIONS = enduringKey(booleanKey("apply-nukkit-attachment-permissions", true));

    /**
     * If BungeeCord configured permissions are being applied. This setting is ignored on other platforms.
     */
    public static final ConfigKey<Boolean> APPLY_BUNGEE_CONFIG_PERMISSIONS = enduringKey(booleanKey("apply-bungee-config-permissions", false));

    /**
     * If Sponge's implicit permission inheritance system should be applied
     */
    public static final ConfigKey<Boolean> APPLY_SPONGE_IMPLICIT_WILDCARDS = enduringKey(booleanKey("apply-sponge-implicit-wildcards", true));

    /**
     * If Sponge default subjects should be applied
     */
    public static final ConfigKey<Boolean> APPLY_SPONGE_DEFAULT_SUBJECTS = enduringKey(booleanKey("apply-sponge-default-subjects", true));


    /**
     * If a final sort according to "inheritance rules" should be performed after the traversal algorithm
     * has resolved the inheritance tree
     */
    public static final ConfigKey<Boolean> POST_TRAVERSAL_INHERITANCE_SORT = booleanKey("post-traversal-inheritance-sort", false);



    /**
     * If log notifications are enabled
     */
    public static final ConfigKey<Boolean> LOG_NOTIFY = booleanKey("log-notify", true);

    /**
     * If auto op is enabled. Only used by the Bukkit platform.
     */
    public static final ConfigKey<Boolean> AUTO_OP = enduringKey(booleanKey("auto-op", false));

    /**
     * If server operators should be enabled. Only used by the Bukkit platform.
     */
    public static final ConfigKey<Boolean> OPS_ENABLED = enduringKey(customKey(c -> !AUTO_OP.get(c) && c.getBoolean("enable-ops", true)));

    /**
     * If server operators should be able to use LuckPerms commands by default. Only used by the Bukkit platform.
     */
    public static final ConfigKey<Boolean> COMMANDS_ALLOW_OP = enduringKey(booleanKey("commands-allow-op", true));

    /**
     * If Vault lookups for offline players on the main server thread should be enabled
     */
    public static final ConfigKey<Boolean> VAULT_UNSAFE_LOOKUPS = booleanKey("vault-unsafe-lookups", false);

    /**
     * Controls which group LuckPerms should use for NPC players when handling Vault requests
     */
    public static final ConfigKey<String> VAULT_NPC_GROUP = stringKey("vault-npc-group", "default");

    /**
     * Controls how LuckPerms should consider the OP status of NPC players when handing Vault requests
     */
    public static final ConfigKey<Boolean> VAULT_NPC_OP_STATUS = booleanKey("vault-npc-op-status", false);

    /**
     * If the vault server option should be used
     */
    public static final ConfigKey<Boolean> USE_VAULT_SERVER = booleanKey("use-vault-server", true);

    /**
     * The name of the server to use for Vault.
     */
    public static final ConfigKey<String> VAULT_SERVER = customKey(c -> {
        // default to true for backwards compatibility
        if (USE_VAULT_SERVER.get(c)) {
            return c.getString("vault-server", "global").toLowerCase();
        } else {
            return SERVER.get(c);
        }
    });

    /**
     * If Vault should apply global permissions
     */
    public static final ConfigKey<Boolean> VAULT_INCLUDING_GLOBAL = booleanKey("vault-include-global", true);

    /**
     * If any worlds provided with Vault lookups should be ignored
     */
    public static final ConfigKey<Boolean> VAULT_IGNORE_WORLD = booleanKey("vault-ignore-world", false);

    /**
     * If Vault debug mode is enabled
     */
    public static final ConfigKey<Boolean> VAULT_DEBUG = booleanKey("vault-debug", false);

    /**
     * The prefix for any SQL tables
     */
    public static final ConfigKey<String> SQL_TABLE_PREFIX = enduringKey(stringKey("data.table_prefix", "luckperms_"));

    /**
     * The prefix for any MongoDB collections
     */
    public static final ConfigKey<String> MONGODB_COLLECTION_PREFIX = enduringKey(stringKey("data.mongodb_collection_prefix", ""));

    /**
     * MongoDB ClientConnectionURI to override default connection options
     */
    public static final ConfigKey<String> MONGODB_CONNECTION_URI = enduringKey(stringKey("data.mongodb_connection_URI", ""));

    /**
     * If storage files should be monitored for changes
     */
    public static final ConfigKey<Boolean> WATCH_FILES = booleanKey("watch-files", true);

    /**
     * If split storage is being used
     */
    public static final ConfigKey<Boolean> SPLIT_STORAGE = enduringKey(booleanKey("split-storage.enabled", false));


    /**
     * The name of the messaging service in use, or "none" if not enabled
     */
    public static final ConfigKey<String> MESSAGING_SERVICE = enduringKey(lowercaseStringKey("messaging-service", "none"));

    /**
     * If updates should be automatically pushed by the messaging service
     */
    public static final ConfigKey<Boolean> AUTO_PUSH_UPDATES = enduringKey(booleanKey("auto-push-updates", true));

    /**
     * If LuckPerms should push logging entries to connected servers via the messaging service
     */
    public static final ConfigKey<Boolean> PUSH_LOG_ENTRIES = enduringKey(booleanKey("push-log-entries", true));

    /**
     * If LuckPerms should broadcast received logging entries to players on this platform
     */
    public static final ConfigKey<Boolean> BROADCAST_RECEIVED_LOG_ENTRIES = enduringKey(booleanKey("broadcast-received-log-entries", false));

    /**
     * If redis messaging is enabled
     */
    public static final ConfigKey<Boolean> REDIS_ENABLED = enduringKey(booleanKey("redis.enabled", false));

    /**
     * The address of the redis server
     */
    public static final ConfigKey<String> REDIS_ADDRESS = enduringKey(stringKey("redis.address", null));

    /**
     * The password in use by the redis server, or an empty string if there is no passworld
     */
    public static final ConfigKey<String> REDIS_PASSWORD = enduringKey(stringKey("redis.password", ""));

    /**
     * The URL of the bytebin instance used to upload data
     */
    public static final ConfigKey<String> BYTEBIN_URL = stringKey("bytebin-url", "https://bytebin.lucko.me/");

    /**
     * The URL of the web editor
     */
    public static final ConfigKey<String> WEB_EDITOR_URL_PATTERN = stringKey("web-editor-url", "https://luckperms.github.io/editor/");

    /**
     * The URL of the verbose viewer
     */
    public static final ConfigKey<String> VERBOSE_VIEWER_URL_PATTERN = stringKey("verbose-viewer-url", "https://luckperms.github.io/verbose/");

    /**
     * The URL of the tree viewer
     */
    public static final ConfigKey<String> TREE_VIEWER_URL_PATTERN = stringKey("tree-viewer-url", "https://luckperms.github.io/treeview/");

    private static final Map<String, ConfigKey<?>> KEYS;
    private static final int SIZE;

    static {
        Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
        Field[] values = ConfigKeys.class.getFields();
        int i = 0;

        for (Field f : values) {
            // ignore non-static fields
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            // ignore fields that aren't configkeys
            if (!ConfigKey.class.equals(f.getType())) {
                continue;
            }

            try {
                // get the key instance
                ConfigKeyTypes.BaseConfigKey<?> key = (ConfigKeyTypes.BaseConfigKey<?>) f.get(null);
                // set the ordinal value of the key.
                key.ordinal = i++;
                // add the key to the return map
                keys.put(f.getName(), key);
            } catch (Exception e) {
                throw new RuntimeException("Exception processing field: " + f, e);
            }
        }

        KEYS = ImmutableMap.copyOf(keys);
        SIZE = i;
    }

    /**
     * Gets a map of the keys defined in this class.
     *
     * <p>The string key in the map is the {@link Field#getName() field name}
     * corresponding to each key.</p>
     *
     * @return the defined keys
     */
    public static Map<String, ConfigKey<?>> getKeys() {
        return KEYS;
    }

    /**
     * Gets the number of defined keys.
     *
     * @return how many keys are defined in this class
     */
    public static int size() {
        return SIZE;
    }

}