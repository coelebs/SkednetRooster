package nl.vincentkriek.skednet;

public class FreeRequest {
	@SuppressWarnings("unused")
	private static final String TAG = "nl.vincentkriek.skednet";

	private String start;
	private String end;
	private String reason;
	private String status;
	
	public FreeRequest(String start, String end, String reason, String status) {
		setStart(start);
		setEnd(end);
		this.status = status;
		this.reason = reason;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getReason() {
		return reason;
	}
	
	public String toString() {
		return this.start + " - " + this.end + " : " + this.reason + " | " + this.status;
	}
}
