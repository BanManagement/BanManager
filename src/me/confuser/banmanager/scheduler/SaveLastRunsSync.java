package me.confuser.banmanager.scheduler;

import me.confuser.banmanager.BanManager;

public class SaveLastRunsSync implements Runnable {

	private BanManager plugin;

	public SaveLastRunsSync(BanManager banManager) {
		plugin = banManager;
	}

	public void run() {
		plugin.schedulerFileConfig.set("lastChecked.bans", BansAsync.getLastRun());
		plugin.schedulerFileConfig.set("lastChecked.external", ExternalAsync.getLastRun());
		plugin.schedulerFileConfig.set("lastChecked.ipbans", IpBansAsync.getLastRun());
		plugin.schedulerFileConfig.set("lastChecked.mutes", MuteAsync.getLastRun());
		
		
		plugin.schedulerConfig.saveConfig();
	}

}
