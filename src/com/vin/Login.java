package com.vin;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Login extends Activity implements Runnable{
	private static final String BASEURL = "http://www.skednet.nl/";
	private static final String COMPANY = "companyid";
	private static final String LOCATION = "locationid";
	private static final String BADGE = "badge";
	private static final String PASSWORD = "password";
	
	private DefaultHttpClient httpClient;
	private ArrayList<Workday> workdays;
	
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
				
				login();
			}

		});
        
    }
       
	private void login(String username, String password) {
		// TODO Auto-generated method stub
 
        workdays = new ArrayList<Workday>();
        
        httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BASEURL + "login.php");
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(COMPANY, "1")); //McDonald's is 1, for testing purposes
        params.add(new BasicNameValuePair(LOCATION, "1015")); //Location HSW & Markt
        params.add(new BasicNameValuePair(BADGE, "024"));
        params.add(new BasicNameValuePair(PASSWORD, "vkB61Tuo"));
        
       ProgressDialog pd = new ProgressDialog(Login.this);
       pd.setTitle("Please wait");
       pd.setMessage("Downloading pages...");
       pd.show();
        
        String s = null;
        try {
        	httpPost.setEntity(new  UrlEncodedFormEntity(params, HTTP.UTF_8));
        
        	HttpResponse response = httpClient.execute(httpPost);

        	//List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        	
        	HttpGet httpGet = new HttpGet(BASEURL + "index.php?page=pschedule");
        	response = httpClient.execute(httpGet);
        	
        	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        	String buf;
        	s = "";
        	while((buf = reader.readLine()) != null) {
        		s += buf;
        	}
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
        pd.setMessage("Parsing pages...");
        
        TextView tv = (TextView)findViewById(R.id.text);
        
        Document doc = Jsoup.parse(s);
        Elements elements = doc.select("table[width=825px]");
        ListIterator<Element> iterator = elements.listIterator();
        
        s = "";
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
        
        for(Workday day : workdays) {
        	s += day.getDate() + " " + day.getDay() + " " + day.getTime() + " " + day.getStation() + "\n";
        }
       
        pd.cancel();
        tv.setText(s);
      
    }  

	@Override
	public void run() {
		// TODO Auto-generated method stub
		login("pee", "now");
	}
}