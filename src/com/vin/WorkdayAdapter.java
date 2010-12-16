package com.vin;

import java.util.ArrayList;

import android.database.DataSetObserver;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView row = new TextView(parent.getContext());
		row.setText(workdays.get(position).toString());
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
