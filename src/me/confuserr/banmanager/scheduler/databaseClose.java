package me.confuserr.banmanager.scheduler;

import me.confuserr.banmanager.BanManager;

public class databaseClose implements Runnable {
	
	private BanManager plugin;

	public databaseClose(BanManager banManager) {
		plugin = banManager;
	}

	@Override
	public void run() {
		// Tried to make it thread safe by only closing it if a query is not being executed!
		// Look inside Database to see more info
		plugin.dbLogger.closeConnection();
	}
}
