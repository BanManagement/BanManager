package me.confuser.banmanager.configs;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.configs.Config;

public class MessagesConfig extends Config<BanManager> {

	public MessagesConfig() {
		super("messages.yml");
	}

	public void afterLoad() {
		Message.load(conf);
	}

    public void onSave() {

    }

}
