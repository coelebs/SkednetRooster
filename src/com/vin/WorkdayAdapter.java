package com.vin;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class WorkdayAdapter implements ListAdapter {
	private ArrayList<Workday> workdays;
	private ArrayList<DataSetObserver> observers;
	
	public WorkdayAdapter(ArrayList<Workday> workdays) {
		this.workdays = workdays;
		observers = new ArrayList<DataSetObserver>();
	}
	
	@Override
	public int getCount() {
		return workdays.size();
	}

	@Override
	public Object getItem(int position) {
		return workdays.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	/**
	 * Returns a view that represents an item in the arraylist.
	 * If it is a weeknumber object and weeknumbers are disabled in the settings
	 * it will return an empty view
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
		TextView row = new TextView(parent.getContext());
		Workday day = workdays.get(position); 
		if(day.toString().equals(" \n ") && day.getWeeknumber().length() != 0) {
			if(day.getWeeknumber().length() != 0 && !pref.getBoolean("weeknumbers", false))
				//This allows me to add a empty line, needs listSeperator to be gone
        		return new View(parent.getContext()); 
			row.setText(day.getWeeknumber());
		} else {
			row.setTextColor(0xFFFFFFFF);
			row.setText(day.toString());
		}
		return row;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return workdays.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		observers.remove(observer);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

}
