package com.vin;

import java.util.ArrayList;

public class Week {
	private ArrayList<Day> days;
	private int number;
	
	public Week() {
		days = new ArrayList<Day>();
	}
	
	public void addDay(Day day) {
		days.add(day);
	}
	
	public Day getDay(int index) {
		return days.get(index);
	}
	
	public ArrayList<Day> getDays() {
		return days;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}