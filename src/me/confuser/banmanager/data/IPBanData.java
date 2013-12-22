package me.confuser.banmanager.data;

public class IPBanData {
	
	private String banned;
	private long expires;
	private String reason;
	private long time;
	private String by;
	
	public IPBanData(String name, String bannedBy, String reason, long time, long expires) {
		banned = name;
		this.expires = expires;
		this.reason = reason;
		by = bannedBy;
		this.time = time;
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
