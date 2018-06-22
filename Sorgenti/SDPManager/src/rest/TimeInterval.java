package rest;


/** Classe per quando si riceve un intervallo di tempo
 * per eseguire le query sulle misurazioni
 */
public class TimeInterval {
	
	private long start;
	private long end;
	
	public TimeInterval(String interval) throws NumberFormatException {
		String[] arr = interval.split("-");
		start = Long.parseLong(arr[0]);
		end = Long.parseLong(arr[1]);
	}

	public long getStart() {
		return start;
	}

	public void setStart(long begin) {
		this.start = begin;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}
	
}
