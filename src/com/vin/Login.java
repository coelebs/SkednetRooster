package com.vin;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.NameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
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
	
	private ProgressDialog pd;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pd = new ProgressDialog(Login.this);
				pd.setMessage("Logging in");
				pd.show();
				Thread thread = new Thread(Login.this);
				thread.start();
			}
		});
        
        Schedule.delCache(getApplicationContext());
    }
       
	private boolean login(String username, String password) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(pref.contains(Constants.BADGE) && pref.contains(Constants.PASSWORD))
			return true;
		
		httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Constants.BASEURL + "login.php");
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(Constants.COMPANY, "1")); //McDonald's is 1, for testing purposes
        params.add(new BasicNameValuePair(Constants.LOCATION, "1015")); //Location HSW & Markt
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

	@Override
	public void run() {
		EditText edit = (EditText)findViewById(R.id.badge_input);
		String username = edit.getText().toString();
		
		edit = (EditText)findViewById(R.id.password_input);
		String password = edit.getText().toString();
		
		
		if(login(username, password)) {
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