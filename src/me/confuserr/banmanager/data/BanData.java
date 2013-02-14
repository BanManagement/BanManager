package me.confuserr.banmanager.data;

public class BanData {
	
	private String banned;
	private long expires;
	private String reason;
	private long time;
	private String by;
	
	public BanData(String dbanned, long dexpires, String dreason, long dtime, String dby) {
		banned = dbanned;
		expires = dexpires;
		reason = dreason;
		by = dby;
	}
	
	public String getBanned() {
		return banned;
	}
	
	public long getExpires() {
		return expires;
	}
	
	public String getReason() {
		return reason;
	}
	
	public long getTime() {
		return time;
	}
	
	public String getBy() {
		return by;
	}
}
