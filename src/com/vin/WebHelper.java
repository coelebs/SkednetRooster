package com.vin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class WebHelper {
	private static final String TAG = "WEBHELPER";

	public static String post(String path, List<NameValuePair> data) {
		String s = "";
        try {
        	DefaultHttpClient httpClient = new DefaultHttpClient();
        	HttpPost httpPost = new HttpPost(Constants.BASEURL + "login.php");
        	httpPost.setEntity(new  UrlEncodedFormEntity(data, HTTP.UTF_8));
        	HttpResponse response = httpClient.execute(httpPost);
        	        	
        	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        	String buf;
        	s = "";
        	while((buf = reader.readLine()) != null) {
        		s += buf;
        	}
        } catch(Exception e) {
        	Log.e(TAG, e.getMessage());
        }
    	return s;
	}
}
