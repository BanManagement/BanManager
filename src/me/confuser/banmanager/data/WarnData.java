package me.confuser.banmanager.data;

public class WarnData {

	private String banned;
	private String reason;
	private long time;
	private String by;

	public WarnData(String name, String warnedBy, String reason, long time) {
		banned = name;
		this.reason = reason;
		by = warnedBy;
		this.time = time;
	}

	public String getWarned() {
		return banned;
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
