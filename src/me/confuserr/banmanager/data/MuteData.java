package me.confuserr.banmanager.data;

public class MuteData {
	
	private String muted;
	private long expires;
	private String reason;
	private long time;
	private String by;
	
	public MuteData(String dmuted, long dexpires, String dreason, long dtime, String dby) {
		muted = dmuted;
		expires = dexpires;
		reason = dreason;
		by = dby;
	}
	
	public String getMuted() {
		return muted;
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
