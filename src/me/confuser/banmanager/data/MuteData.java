package me.confuser.banmanager.data;

public class MuteData {
	
	private String muted;
	private long expires;
	private String reason;
	private long time;
	private String by;
	
	public MuteData(String name, String bannedBy, String reason, long time, long expires) {
		muted = name;
		this.expires = expires;
		this.reason = reason;
		by = bannedBy;
		this.time = time;
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
