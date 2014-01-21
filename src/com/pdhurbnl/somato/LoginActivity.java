package com.pdhurbnl.somato;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void login(View view)
	{
		// Get login info
		EditText emailT = (EditText) findViewById(R.id.loginEmail);
		String email = emailT.getText().toString();
		
		EditText passwordField = (EditText) findViewById(R.id.PasswordField);
		String password = passwordField.getText().toString();
		
		// Please wait message
		Toast.makeText(this,
				"Please wait, logging in...", Toast.LENGTH_SHORT)
				.show();
		
		// Set up params
		RequestParams params = new RequestParams();
		params.put("email", email);
		params.put("password", password);
		
		// Make HTTP request
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/user/login.php",
			params,
			new AsyncHttpResponseHandler()
			{
			    @Override
			    public void onSuccess(String response)
			    {
			        // Figure out what to do with result
			    	if (response.equals("EMAIL_INCORRECT"))
			    	{
			    		Toast
			    			.makeText(LoginActivity.this,
			    					"Your email did not match our records.",
			    					Toast.LENGTH_SHORT
			    			).show();
			    	}
			    	else if (response.equals("PASSWORD_INCORRECT"))
			    	{
			    		Toast
			    			.makeText(LoginActivity.this,
			    					"Your password was incorrect.",
			    					Toast.LENGTH_SHORT
			    			).show();
			    	}
			    	else
			    	{
			    		// Store user id in sharedpreferences
				    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				    	SharedPreferences.Editor editor = sharedPrefs.edit();
				    	editor.putString("user_id", response);
				    	editor.commit();
				    	
				    	// Start main activity
				    	Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				    	startActivity(intent);
			    	}
			    }
			}
		);
	}
	
	public void startRegister(View view)
	{
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);
	}

}
