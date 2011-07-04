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
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import nl.vincentkriek.skednet.schedule.Day;
import nl.vincentkriek.skednet.schedule.Week;

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
	private static final String TAG = "nl.vincentkriek.skednet";
	
	/**
	 * Retrieves all the current workdays from BASEURL, caches them and uses the cache if available
	 * It also adds weeknumbers as if they were workdays
	 * @param context The application context, used to retrieve various application settings
	 * @return A boolean to indicate wether it succeeded
	 */
	public static ArrayList<Week> getWorkdays(Context context) {
		ArrayList<Week> workdays = readWorkdays(context);
		if(workdays.isEmpty()) {
	        String html = getAuthorized("index.php?page=freerequest2", context);
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
	public static ArrayList<Week> readWorkdays(Context context) {
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
	public static void writeWorkdays(ArrayList<Week> workdays, Context context) {
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
	public static ArrayList<Week> parseSchedule(String html) {
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
	
	public static ArrayList<FreeRequest> parseDaysOff(String html) {
		ArrayList<FreeRequest> days = new ArrayList<FreeRequest>();
		
		Document doc = Jsoup.parse(html);
		Elements elements = doc.select("table[style=width:100%;border-collapse: collapse; border: 0px solid #01376D]" +
				" td[style*=text-align:left] table[style=width:100%] > tr:gt(0)");
	
    	ListIterator<Element> iterator1 = elements.listIterator();
     	while(iterator1.hasNext()) {
    		Element row = iterator1.next();
    		
    		days.add(new FreeRequest(row.child(0).text(), row.child(1).text(), 
    									row.child(2).text(), row.child(3).text()));
    		
    	}
    	
		return days;
	}

	/**
	 * Make a GET request to a page
	 * @param path the path (not including www.skednet.nl/)
	 * @param context The current application context
	 * @return The page source
	 */
	public static String getAuthorized(String path, Context context) {
		String html = null;
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String username = pref.getString(Constants.BADGE, "");
		String password = pref.getString(Constants.PASSWORD, "");
		String location = pref.getInt(Constants.LOCATION, 0) + "";
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Constants.BASEURL + "login.php");
        
        List<NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicNameValuePair(Constants.COMPANY, "1")); //McDonald's is 1, for testing purposes
        data.add(new BasicNameValuePair(Constants.LOCATION, location)); //Location HSW & Markt
        data.add(new BasicNameValuePair(Constants.BADGE, username));
        data.add(new BasicNameValuePair(Constants.PASSWORD, password));
        
        WebHelper.post(Constants.BASEURL + path, data);
        try {
        	httpPost.setEntity(new  UrlEncodedFormEntity(data, HTTP.UTF_8));
        	httpClient.execute(httpPost);
        	
        	HttpResponse response = httpClient.execute(new HttpGet(Constants.BASEURL + path));
        	
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
	 * Fetches all the locations and their id from the BASEURL, caches it, and uses the cache if available
	 * @return A map with the location String as key and id as value
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Integer> getLocations(Context context) {
		File f = new File(context.getCacheDir(), "locations");
		ObjectInputStream in;
		Map<String, Integer> map = null;
		try {
			in = new ObjectInputStream(new FileInputStream(f));
			map = (Map<String, Integer>) in.readObject();
		} catch (StreamCorruptedException e) {
        	Log.e(TAG, e.getMessage());
		} catch (FileNotFoundException e) {
        	Log.e(TAG, e.getMessage());
		} catch (IOException e) {
        	Log.e(TAG, e.getMessage());
		} catch (ClassNotFoundException e) {
        	Log.e(TAG, e.getMessage());
		}
			
		if(map == null) {
			map = new HashMap<String, Integer>();
			
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair(Constants.COMPANY, "1")); //McDonald's is 1, for testing purposes
	        
	        String s = WebHelper.post(Constants.BASEURL + "login.php", params);
	        Document document = Jsoup.parse(s);
	        Elements elements = document.select("select[name=locationid] > option");
	        ListIterator<Element> iterator = elements.listIterator();
	        while(iterator.hasNext()) {
	        	Element element = iterator.next();
	        	if(element.text().equals("")) 
	        		continue;
	        	
	        	map.put(element.text(), Integer.parseInt(element.attr("value")));
	        }
	        ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(new FileOutputStream(f));
		        out.writeObject(map);  
			} catch (FileNotFoundException e) {
	        	Log.e(TAG, e.getMessage());
			} catch (IOException e) {
	        	Log.e(TAG, e.getMessage());
			}
		}
        return map;
	}
	
	/**
     * Logs the user in, uses the classes username and password because of threads
     * @return Boolean to indicate if the login succeeded
     */
	public static boolean login(Context context, String username, String password) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Constants.BASEURL + "login.php");
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(Constants.COMPANY, "1")); //McDonald's is 1, for testing purposes
        params.add(new BasicNameValuePair(Constants.LOCATION, pref.getInt(Constants.LOCATION, 0) + "")); 
        params.add(new BasicNameValuePair(Constants.BADGE, username));
        params.add(new BasicNameValuePair(Constants.PASSWORD, password));
        
        try {
        	httpPost.setEntity(new  UrlEncodedFormEntity(params, HTTP.UTF_8));
        	httpClient.execute(httpPost);
        	if(httpClient.getCookieStore().getCookies().isEmpty()) {
        		return false;
        	} else {
        		Editor editor = pref.edit();
        		editor.putString(Constants.BADGE, username);
        		editor.putString(Constants.PASSWORD, password);
        		editor.commit();
        		return true;
        	}
        } catch(Exception e) {
        	Log.e(TAG, e.getMessage());
        }
        
        return false;
       
    }  
	
	/**
	 * Delete all the information about the current logged in user
	 */
	public static void logOut(Context context) {
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
	
	/**
	 * Clears the cache
	 * @param context The current applicationcontext
	 */
	public static void delCache(Context context) {
		File f = new File(context.getCacheDir(), "schedule");
		f.delete();
	}
	 
}