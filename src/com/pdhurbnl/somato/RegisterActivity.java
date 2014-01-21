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

public class RegisterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void register(View view)
	{
		// Get texts
		EditText emailT = (EditText) findViewById(R.id.UserField);
		EditText firstNameT = (EditText) findViewById(R.id.FirstNameField);
		EditText lastNameT = (EditText) findViewById(R.id.LastNameField);
		EditText passwordT = (EditText) findViewById(R.id.PasswordField);
		
		// Get strings
		String email = emailT.getText().toString();
		String firstName = firstNameT.getText().toString();
		String lastName = lastNameT.getText().toString();
		String password = passwordT.getText().toString();
		
		// Please wait message
		Toast.makeText(this, 
				"Please wait, registering...",
				Toast.LENGTH_SHORT
				).show();
		
		// Set up params
		RequestParams params = new RequestParams();
		params.put("email", email);
		params.put("first_name", firstName);
		params.put("last_name", lastName);
		params.put("password", password);
		
		// Make HTTP Request
		AsyncHttpClient client = new AsyncHttpClient();
		try
		{
		client.post(
			"http://somato.pauldilyard.com/user/register.php",
			params,
			new AsyncHttpResponseHandler()
			{
			    @Override
			    public void onSuccess(String response)
			    {
			        // Store user id in sharedpreferences
			    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			    	SharedPreferences.Editor editor = sharedPrefs.edit();
			    	editor.putString("user_id", response);
			    	editor.commit();
			    	
			    	// Start the main activity
			    	Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
			    	startActivity(intent);
			    }
			}
		);
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

}
