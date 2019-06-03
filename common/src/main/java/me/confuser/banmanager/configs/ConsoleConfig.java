package me.confuser.banmanager.configs;

import java.util.UUID;
import lombok.Getter;
import me.confuser.banmanager.common.config.ConfigKeyTypes;
import me.confuser.banmanager.common.config.ConfigKeys;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;

public class ConsoleConfig {

    @Getter
    private String name;
    @Getter
    private UUID uuid;

    BanManagerPlugin plugin;

    public ConsoleConfig() {
        super("console.yml");
    }

    @Override
    public void afterLoad() {
        if (plugin.getConfiguration().get(ConfigKeys.CONSOLE_UUID).equals("0")) {
            uuid = UUID.randomUUID();
            save();
        } else {
            uuid = UUID.fromString(plugin.getConfiguration().get(ConfigKeys.CONSOLE_UUID));
        }

        name = plugin.getConfiguration().get(ConfigKeys.CONSOLE_NAME);
    }

    @Override
    public void onSave() {
        if (uuid == null) {
            return;
        }

        plugin.getConfiguration().set((ConfigKeyTypes.FunctionalKey)ConfigKeys.CONSOLE_UUID, uuid.toString());
        plugin.getConfiguration().set((ConfigKeyTypes.FunctionalKey)ConfigKeys.CONSOLE_NAME, name);
    }
}