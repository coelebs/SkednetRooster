package com.vin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity implements Runnable{
	private static final String TAG = "SKEDNETLOGIN";
	
	private DefaultHttpClient httpClient;
	
	private String username;
	private String password;
	
	private ProgressDialog pd;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(pref.contains(Constants.BADGE) && pref.contains(Constants.PASSWORD)) {
			startActivity(new Intent(getApplicationContext(), Schedule.class));
		} else {
			showDialogs();
		}
		
        Schedule.delCache(getApplicationContext());
    }
       
	private boolean login() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		httpClient = new DefaultHttpClient();
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
	
	private void showDialogs() {
        final Dialog loginDialog = new Dialog(this);

        loginDialog.setContentView(R.layout.login);
        loginDialog.setTitle("login");

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        EditText edit = (EditText)loginDialog.findViewById(R.id.badge_input);
        edit.setText(pref.getString(Constants.BADGE, ""));
        
        edit = (EditText)loginDialog.findViewById(R.id.password_input);
        edit.setText(pref.getString(Constants.PASSWORD, ""));
        
        Button submit = (Button)loginDialog.findViewById(R.id.submit);
        submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pd = new ProgressDialog(Login.this);
				pd.setMessage("Logging in");
				pd.show();
				
				username = ((EditText)loginDialog.findViewById(R.id.badge_input)).getText().toString();
				password = ((EditText)loginDialog.findViewById(R.id.password_input)).getText().toString();
				
				Thread thread = new Thread(Login.this);
				thread.start();
			}
		});
        
        final Map<String, Integer> locations = getLocations();
        String fake[] = new String[1];
        final String keys[] = locations.keySet().toArray(fake);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_location);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				loginDialog.show();				
			}
		});
        
        builder.setSingleChoiceItems(keys, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
				edit.putInt(Constants.LOCATION, locations.get(keys[which]));
				edit.commit();
			}
		});
        
        builder.show();
	}

	private Map<String, Integer> getLocations() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		
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

        return map;
	}
	
	@Override
	public void run() {		
		if(login()) {
			handler.sendEmptyMessage(RESULT_OK);
		} else {
			handler.sendEmptyMessage(RESULT_CANCELED);
		}
	}
	
	private Handler handler = new Handler() {
		
		@Override
		public void handleMessage(android.os.Message msg) {
			if(msg.what == RESULT_OK) {
				pd.cancel();	
				startActivity(new Intent(getApplicationContext(), Schedule.class));
			} else {
				pd.cancel();
				finish();
			}
		}
	};

}