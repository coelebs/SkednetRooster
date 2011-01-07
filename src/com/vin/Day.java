package com.vin;

import java.io.Serializable;

public class Day implements Serializable {
	/**
	 * ID for serialization
	 */
	private static final long serialVersionUID = -785496744501147274L;
	private String day;
	private String date;
	private String time;
	private String station;
	
	public Day(String day, String date, String time, String station) {
		this.setDay(day);
		this.setDate(date);
		this.setTime(time);
		this.setStation(station);
	}

	public Day() {
	}
	
	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return date;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public String getStation() {
		return station;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTime() {
		return time;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getDay() {
		return day;
	}
	
	/**
	 * Checks the contents of day, date, time, and station to check if this is a filled workday
	 * @return A boolean that indicates if this is a workday or not
	 */
	public boolean isWorkday() {
		return (day.length() != 0 && date.length() != 0 &&
			station.length() != 0 && time.length() != 0);
	}

	/**
	 * A string that represents the current object
	 * in the format "day date\ntime station"
	 */
	public String toString() {
		return day + " " + date + " " + time + " " + station;
	}
}
