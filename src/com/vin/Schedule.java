package com.vin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Schedule extends Activity {
	private static final String TAG = "SCHEDULE";
	
	private ArrayList<Week> workdays;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule);
		
		workdays = new ArrayList<Week>();
		pd = new ProgressDialog(this);
		
		ScheduleFetcher fetch = new ScheduleFetcher();
		fetch.execute(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//If workdays is filled, reload the page. Used to reload after settings...
		//if(!workdays.isEmpty()) how now?
	}

	/**
	 * Clears the cache
	 * @param context The current applicationcontext
	 */
	public static void delCache(Context context) {
		File f = new File(context.getCacheDir(), "schedule");
		f.delete();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.logout:
		    	Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
		    	edit.remove(Constants.BADGE);
		    	edit.remove(Constants.PASSWORD);
		    	edit.remove(Constants.LOCATION);
		    	edit.commit();
		    	finish();
		    	startActivity(new Intent(getApplicationContext(), Login.class));
		    	return true;
		    case R.id.settings:
		    	startActivity(new Intent(getApplicationContext(), Settings.class));
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public class ScheduleFetcher extends AsyncTask<Context, Integer, Object> {

		/**
		 * Retrieves all the current workdays from BASEURL, caches them and uses the cache if available
		 * It also adds weeknumbers as if they were workdays
		 * @return A boolean to indicate wether it succeeded
		 */
		@Override
		protected Object doInBackground(Context... params) {
			workdays = Skednet.readWorkdays(getApplicationContext());
			if(workdays.isEmpty()) {
				publishProgress(1);
		        String html = Skednet.getAuthorized("index.php?page=pschedule", getApplicationContext());
		        publishProgress(2);
		        workdays = Skednet.parseSchedule(html);
	            Skednet.writeWorkdays(workdays, getApplicationContext());
			}
	        return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			switch(values[0]) {
				case 0:

				case 1:
					pd.setMessage("Loading page...");
					pd.show();
					break;
				case 2:
					pd.setMessage("Processing page...");
					pd.show();
					break;
			}
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			for(Week week: workdays) {
				View root = getLayoutInflater().inflate(R.layout.week, null);

				TextView tv = (TextView) root.findViewById(R.id.weeknumber);
				tv.setText("Week " + week.getNumber());
				
				String s = "";
				Iterator<Day> days = week.getDays().iterator();
				while(days.hasNext()) {
					s += days.next().toString();
					if(days.hasNext())
						s += "\n";
				}
				
				tv = (TextView) root.findViewById(R.id.weekdays);
				tv.setText(s);
				
				((LinearLayout)findViewById(R.id.weekday_layout)).addView(root);
			}
			pd.cancel();
		}
		
	}
}
