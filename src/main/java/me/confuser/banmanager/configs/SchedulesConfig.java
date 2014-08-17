package me.confuser.banmanager.configs;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.configs.Config;

public class SchedulesConfig extends Config<BanManager> {
	private int expiresCheck = 300;
	private int playerBans = 30;
	private int playerMutes = 30;
	private int ipBans = 30;
	private int external = 120;
	private int saveLastRuns = 60;
	
	

	public SchedulesConfig() {
		super("schedules.yml");
	}

	@Override
	public void afterLoad() {
		
	}

	@Override
	public void onSave() {
		// TODO Auto-generated method stub
		//conf.set("scheduler.expiresCheck", arg1)
	}

}
