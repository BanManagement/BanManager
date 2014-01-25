package me.confuser.banmanager.tabcomplete;

import me.confuser.banmanager.BanManager;

public class RegisterTabCompletes {
	public RegisterTabCompletes(BanManager plugin) {
		plugin.getCommand("unban").setTabCompleter(new UnBanTabComplete());
	}
}
