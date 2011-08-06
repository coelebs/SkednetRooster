package nl.vincentkriek.skednet.schedule;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class Day implements Serializable {
	/**
	 * ID for serialization
	 */
	private static final long serialVersionUID = -785496744501147274L;
	private static final String TAG = "nl.vincentkriek.skednet";

	private Date startDate;
	private Date endDate;

	private String station;

	public Day() {
	}
	
	public void setStartDate(String date, String starttime) {
		if (date.length() == 0) {
			return;
		}
		date += " 11 " + starttime;
		
		this.startDate = parseDate(date, date.split(" ")[1].length());
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(String date, String endtime) {
		if (date.length() == 0) {
			return;
		}
		date += " 11 " + endtime;
		
		this.endDate = parseDate(date, date.split(" ")[1].length());
	}

	public void setStation(String station) {
		this.station = station;
	}

	public String getStation() {
		return station;
	}
	
	/**
	 * Checks the contents of day, date, time, and station to check if this is a filled workday
	 * @return A boolean that indicates if this is a workday or not
	 */
	public boolean isWorkday() {
		return startDate != null;
	}

	/**
	 * A string that represents the current object
	 * in the format "day date time station"
	 */
	public String toString() {
		SimpleDateFormat startformat =  new SimpleDateFormat("EEE dd MMM HH:mm", new Locale("nl", "NL"));
		SimpleDateFormat endformat =  new SimpleDateFormat("HH:mm", new Locale("nl", "NL"));

		return startformat.format(startDate) + " - " + endformat.format(endDate) + " " + station;
	}
	
	public String toDayString() {
		SimpleDateFormat startformat =  new SimpleDateFormat("EEE dd MMM", new Locale("nl", "NL"));
		return startformat.format(startDate);
	}
	
	public String toTimeString() {
		SimpleDateFormat startformat =  new SimpleDateFormat("HH:mm", new Locale("nl", "NL"));
		SimpleDateFormat endformat =  new SimpleDateFormat("HH:mm", new Locale("nl", "NL"));

		return startformat.format(startDate) + " - " + endformat.format(endDate) + " " + this.station;
	}
	
	public Date parseDate(String date, int monthLength) {
		String format = "dd ";
		for(int i = 0; i < monthLength; i++) {
			format += "M";
		}
		format += " yy HH:mm";
		
		Locale locale = new Locale("nl", "NL");
		SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
		Date dateobject = null;
		try {
			dateobject = formatter.parse(date);
		} catch (ParseException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return dateobject;
	}
}
