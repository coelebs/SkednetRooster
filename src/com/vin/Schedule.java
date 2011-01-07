package com.vin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Schedule extends Activity {
	private static final String TAG = "SCHEDULE";
	
	private ArrayList<Week> workdays;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule);

		TextView tv = (TextView)findViewById(R.id.weeknumber);
		tv.setText("Week 1");
		
		tv = (TextView)findViewById(R.id.weekdays);
		tv.setText("Watasdfas\nadfadfadsfa\nadfasdfasdfa\nasdfasdfasdfasdf\nadfadfadfasdfasdf\nasdfadfadsf");
		
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
			workdays = readWorkdays();
			String s = null;
			if(workdays.isEmpty()) {
				publishProgress(1);
				
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String username = pref.getString(Constants.BADGE, "");
				String password = pref.getString(Constants.PASSWORD, "");
				
				DefaultHttpClient httpClient = new DefaultHttpClient();
		        HttpPost httpPost = new HttpPost(Constants.BASEURL + "login.php");
		        
		        List<NameValuePair> data = new ArrayList<NameValuePair>();
		        data.add(new BasicNameValuePair(Constants.COMPANY, "1")); //McDonald's is 1, for testing purposes
		        data.add(new BasicNameValuePair(Constants.LOCATION, "1015")); //Location HSW & Markt
		        data.add(new BasicNameValuePair(Constants.BADGE, username));
		        data.add(new BasicNameValuePair(Constants.PASSWORD, password));
		        
		        try {
		        	httpPost.setEntity(new  UrlEncodedFormEntity(data, HTTP.UTF_8));
		        	httpClient.execute(httpPost);
		        	
		        	HttpResponse response = httpClient.execute(new HttpGet(Constants.BASEURL + "index.php?page=pschedule"));
		        	
		        	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		        	String buf;
		        	s = "";
		        	while((buf = reader.readLine()) != null) {
		        		s += buf;
		        	}
		        } catch(Exception e) {
		        	Log.e(TAG, e.getMessage());
		        }
			

		        publishProgress(2);
		        Document doc = Jsoup.parse(s);
		        Elements elements = doc.select("table[width=825px]");
		        ListIterator<Element> iterator = elements.listIterator();
		
		        while(iterator.hasNext()) {
		        	Element next = iterator.next();
		        	Elements rows = next.getElementsByTag("tr");
		        	
		        	ListIterator<Element> iterator1 = rows.listIterator();
		        	Week week = new Week();
		        	while(iterator1.hasNext()) {
		        		Element row = iterator1.next();
		            	String weeknumber = row
		            			.select("th[style=border-bottom:1px solid #01376D;background-color:#0059bb;color:#FFFFFF;text-align:center;font-weight:bold")
		            			.text();
		            	
		            	if(weeknumber.length() > 1) {
		            		week.setNumber(Integer.parseInt(weeknumber.replace("Week ", "")));
		            		continue;
		            	}

		            	Day day = new Day();
		            	day.setDay(row.select("td[style=padding-left:2px;width:68px;color:#000000]").text());
		            	day.setDate(row.select("td[style=border-right:1px solid #01376D;width:84px;color:#000000]").text());
		            	day.setTime(row.select("td[style=padding-left:2px;width:82px;border-right: 1px solid #01376D]").text());
		            	day.setStation(row.select("td[style=padding-left:2px;border-right:0px solid #444444;width:40px;]").text());
		            	
		            	if(day.isWorkday())
		            		week.addDay(day);	
		        	}
		        	workdays.add(week);
		        }
	            writeWorkdays(workdays);
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
			TextView tv = (TextView)findViewById(R.id.weeknumber);
			tv.setText("Week " + workdays.get(0).getNumber());
			
			String s = "";
			Iterator<Day> days = workdays.get(0).getDays().iterator();
			while(days.hasNext()) {
				s += days.next().toString();
				if(days.hasNext())
					s += "\n";
			}
			
			tv = (TextView)findViewById(R.id.weekdays);
			tv.setText(s);
			pd.cancel();
		}
		
		/**
		 * Caches a workdays arraylist
		 * @param workdays The arraylist with workdays
		 */
		private void writeWorkdays(ArrayList<Week> workdays) {
			File f = new File(getCacheDir(), "schedule");
			ObjectOutputStream  out;
			try {
				out = new ObjectOutputStream(new FileOutputStream(f));
				out.writeObject(workdays);
			} catch (FileNotFoundException e) {
	        	Log.e(TAG, e.getMessage());
			} catch (IOException e) {
	        	Log.e(TAG, e.getMessage());
			}
		}
		
		/**
		 * Reads arraylist with workdays from cache
		 * @return The arraylist with workdays
		 */
		@SuppressWarnings("unchecked")
		private ArrayList<Week> readWorkdays() {
			File f = new File(getCacheDir(), "schedule");
			ObjectInputStream in;
			ArrayList<Week> workdays = new ArrayList<Week>();
			try {
				in = new ObjectInputStream(new FileInputStream(f));
					workdays = (ArrayList<Week>) in.readObject();
			} catch(IOException e) {
	        	Log.e(TAG, e.getMessage());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return workdays;
		}
	}
}
