package com.vin;

public class Workday {
	private String day;
	private String date;
	private String time;
	private String station;
	
	public Workday(String day, String date, String time, String station) {
		this.setDay(day);
		this.setDate(date);
		this.setTime(time);
		this.setStation(station);
	}

	public Workday() {
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
	
	public boolean isEmpty() {
		return day.length() == 0 && date.length() == 0 &&
			station.length() == 0 && time.length() == 0;
	}
	
	public String toString() {
		return day + " " + date + " " + time + " " + station;
	}
}
