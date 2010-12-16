package com.vin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

public class Schedule extends ListActivity implements Runnable {
	private static final String TAG = "SCHEDULE";
	
	private ArrayList<Workday> workdays;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		workdays = new ArrayList<Workday>();
		
		Thread thread = new Thread(this);
		thread.start();
		
	}

	private boolean fillWorkdays() {
		String s = readString();
		if(s.length() == 0) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String username = pref.getString(Constants.BADGE, "");
			String password = pref.getString(Constants.PASSWORD, "");
			
			DefaultHttpClient httpClient = new DefaultHttpClient();
	        HttpPost httpPost = new HttpPost(Constants.BASEURL + "login.php");
	        
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair(Constants.COMPANY, "1")); //McDonald's is 1, for testing purposes
	        params.add(new BasicNameValuePair(Constants.LOCATION, "1015")); //Location HSW & Markt
	        params.add(new BasicNameValuePair(Constants.BADGE, username));
	        params.add(new BasicNameValuePair(Constants.PASSWORD, password));
	        
	        try {
	        	httpPost.setEntity(new  UrlEncodedFormEntity(params, HTTP.UTF_8));
	        	httpClient.execute(httpPost);
	        	
	        	HttpResponse response = httpClient.execute(new HttpGet(Constants.BASEURL + "index.php?page=pschedule"));
	        	
	        	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	        	String buf;
	        	s = "";
	        	while((buf = reader.readLine()) != null) {
	        		s += buf;
	        	}
	        	writeString(s);
	        } catch(Exception e) {
	        	e.printStackTrace();
	        }
		}

        Document doc = Jsoup.parse(s);
        Elements elements = doc.select("table[width=825px]");
        ListIterator<Element> iterator = elements.listIterator();

        while(iterator.hasNext()) {
        	Element next = iterator.next();
        	Elements rows = next.getElementsByTag("tr");
        	
        	ListIterator<Element> iterator1 = rows.listIterator();
        	while(iterator1.hasNext()) {
        		Element row = iterator1.next();
            	Workday day = new Workday();
            	day.setDay(row.select("td[style=padding-left:2px;width:68px;color:#000000]").text());
            	day.setDate(row.select("td[style=border-right:1px solid #01376D;width:84px;color:#000000]").text());
            	day.setTime(row.select("td[style=padding-left:2px;width:82px;border-right: 1px solid #01376D]").text());
            	day.setStation(row.select("td[style=padding-left:2px;border-right:0px solid #444444;width:40px;]").text());
            	if(!day.isEmpty())
            		workdays.add(day);	
        	}
        	
        	

        }

        handler.sendEmptyMessage(0);
        return true;
	}
	
	private Handler handler = new Handler() {
	
		public void handleMessage(android.os.Message msg) {
			getListView().setAdapter(new WorkdayAdapter(workdays));
		}
	};
	
	@Override
	public void run() {
		fillWorkdays();
	}
	
	private void writeString(String s) {
		File f = new File(getCacheDir(), "schedule");
		FileOutputStream out;
		try {
			out = new FileOutputStream(f);
			out.write(s.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String readString() {
		File f = new File(getCacheDir(), "schedule");
		String buf, s = "";
		try {
			BufferedReader input = new BufferedReader(new FileReader(f));
			while((buf = input.readLine()) != null) {
				s += buf;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static void delCache(Context context) {
		File f = new File(context.getCacheDir(), "schedule");
		f.delete();
	}
}
