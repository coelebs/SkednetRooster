package nl.vincentkriek.skednet;

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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Skednet {
	private static final String TAG = "SKEDNET";
	
	/**
	 * Retrieves all the current workdays from BASEURL, caches them and uses the cache if available
	 * It also adds weeknumbers as if they were workdays
	 * @param context The application context, used to retrieve various application settings
	 * @return A boolean to indicate wether it succeeded
	 */
	public static ArrayList<Week> getWorkdays(Context context) {
		ArrayList<Week> workdays = readWorkdays(context);
		if(workdays.isEmpty()) {
	        String html = getAuthorized("index.php?page=pschedule", context);
	        workdays = parseSchedule(html);
            writeWorkdays(workdays, context);
		}
        return workdays;
	}
	
	/**
	 * Reads arraylist with workdays from cache
	 * @param context Application context, used to get the cachedir
	 * @return The arraylist with workdays
	 */
	@SuppressWarnings("unchecked")
	protected static ArrayList<Week> readWorkdays(Context context) {
		ObjectInputStream in;
		ArrayList<Week> workdays = new ArrayList<Week>();
		
		File dir = new File(context.getCacheDir(), "vincentkriek/");
		dir.mkdirs();
		
		File f = new File(dir, "schedule");
		
		if(f.lastModified() < (f.lastModified() - (60 * 60 * 24 * 7))) {
			f.delete();
			return workdays;
		}
		
		try {
			in = new ObjectInputStream(new FileInputStream(f));
			workdays = (ArrayList<Week>) in.readObject();
		} catch(IOException e) {
        	Log.e(TAG, e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return workdays;
	}
	
	/**
	 * Caches a workdays arraylist
	 * @param workdays The arraylist with workdays
	 * @param context Application context, used to get the cachedir
	 */
	protected static void writeWorkdays(ArrayList<Week> workdays, Context context) {
		File dir = new File(context.getCacheDir(), "vincentkriek/");
		dir.mkdirs();
		
		File f = new File(dir, "schedule");
		ObjectOutputStream  out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(f));
			out.writeObject(workdays);
			out.close();
		} catch (FileNotFoundException e) {
        	Log.e(TAG, e.getMessage());
		} catch (IOException e) {
        	Log.e(TAG, e.getMessage());
		}
	}
	
	/**
	 * Parses the skednet page and returns a list of weeks
	 * @param html the skednet schedule page
	 * @return A ArrayList of weeks
	 */
	protected static ArrayList<Week> parseSchedule(String html) {
		ArrayList<Week> workdays = new ArrayList<Week>();
		
        Document doc = Jsoup.parse(html);
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
        return workdays;
	}

	/**
	 * Make a GET request to a page
	 * @param path the path (not including www.skednet.nl/)
	 * @param context The current application context
	 * @return The page source
	 */
	protected static String getAuthorized(String path, Context context) {
		String html = null;
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String username = pref.getString(Constants.BADGE, "");
		String password = pref.getString(Constants.PASSWORD, "");
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Constants.BASEURL + "login.php");
        
        List<NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicNameValuePair(Constants.COMPANY, "1")); //McDonald's is 1, for testing purposes
        data.add(new BasicNameValuePair(Constants.LOCATION, "1015")); //Location HSW & Markt
        data.add(new BasicNameValuePair(Constants.BADGE, username));
        data.add(new BasicNameValuePair(Constants.PASSWORD, password));
        
        WebHelper.post(Constants.BASEURL + "index.php?page=pschedule", data);
        try {
        	httpPost.setEntity(new  UrlEncodedFormEntity(data, HTTP.UTF_8));
        	httpClient.execute(httpPost);
        	
        	HttpResponse response = httpClient.execute(new HttpGet(Constants.BASEURL + "index.php?page=pschedule"));
        	
        	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        	String buf;
        	html = "";
        	while((buf = reader.readLine()) != null) {
        		html += buf;
        	}
        } catch(Exception e) {
        	Log.e(TAG, e.getMessage());
        }
        
    	return html;
	}
	
	/**
	 * Delete all the information about the current logged in user
	 */
	protected static void logOut(Context context) {
    	Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	edit.remove(Constants.BADGE);
    	edit.remove(Constants.PASSWORD);
    	edit.remove(Constants.LOCATION);
    	edit.commit();
    	
		File dir = new File(Environment.getExternalStorageDirectory(), "com.vin/");
		dir.mkdirs();
		File f = new File(dir, "schedule");
		
		f.delete();
	}
	 
}