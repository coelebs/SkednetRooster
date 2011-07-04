package nl.vincentkriek.skednet.activities;

import java.util.Map;

import nl.vincentkriek.skednet.Constants;
import nl.vincentkriek.skednet.R;
import nl.vincentkriek.skednet.Skednet;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements Runnable{
	@SuppressWarnings("unused")
	private static final String TAG = "nl.vincentkriek.skednet";
		
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
		
        Skednet.delCache(getApplicationContext());
    }
	
	/**
	 * Shows a login dialog with username/password and a list of locations
	 */
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
        
        final Map<String, Integer> locations = Skednet.getLocations(getApplicationContext());
		
        String fake[] = new String[1];
        final String keys[] = locations.keySet().toArray(fake);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_location);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				loginDialog.show();				
			}
		});
        
        builder.setSingleChoiceItems(keys, -1, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
				edit.putInt(Constants.LOCATION, locations.get(keys[which]));
				edit.commit();
			}
		});
        
        builder.show();
	}
	
	/**
	 * Runs login() and starts a handler to handle the result
	 */
	public void run() {		
		if(Skednet.login(getApplicationContext(), username, password)) {
			handler.sendEmptyMessage(RESULT_OK);
		} else {
			handler.sendEmptyMessage(RESULT_CANCELED);
		}
	}
	
	/**
	 * A handler to handle the threads result
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			pd.cancel();
			if(msg.what == RESULT_OK) {
				finish();
				startActivity(new Intent(getApplicationContext(), Schedule.class));
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
			}
		}
	};

}