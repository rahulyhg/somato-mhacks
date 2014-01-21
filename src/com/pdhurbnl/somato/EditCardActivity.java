package com.pdhurbnl.somato;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

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

public class EditCardActivity extends Activity {
	
	private String userID;
	
	private EditText linkedin;
	private EditText jobDescription;
	private EditText phone;
	private EditText website;
	private EditText email;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_card);
		
		// get user id
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		this.userID = prefs.getString("user_id", "NOT_LOGGED_IN");
		
		// initialize edit texts
		linkedin = (EditText) findViewById(R.id.linkedin);
		jobDescription = (EditText) findViewById(R.id.JobDescription);
		phone = (EditText) findViewById(R.id.phone);
		website = (EditText) findViewById(R.id.website);
		email = (EditText) findViewById(R.id.email);
		
		RequestParams params = new RequestParams();
		params.put("user_id", userID);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/cards/pullCard.php",
			params,
			new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			        // Parse json result
			    	try
			    	{
						JSONObject jObj = new JSONObject(response);
						String name = jObj.getString("first_name") + " " + jObj.getString("last_name");
						String jobPos = jObj.getString("job_title");
						String phone = jObj.getString("phone");
						String email = jObj.getString("email");
						String website = jObj.getString("website");
						String linkedin = jObj.getString("linkedin");
						String theme = jObj.getString("theme");
						
						ArrayList<String> data = new ArrayList<String>();
						data.add(name);
						data.add(jobPos);
						data.add(phone);
						data.add(email);
						data.add(website);
						data.add(linkedin);
						data.add(theme);
						
						EditCardActivity.this.populateForm(data);
					}
			    	catch (JSONException e)
			    	{
						Toast.makeText(EditCardActivity.this,
								e.getMessage(),
								Toast.LENGTH_LONG).show();
					}
			    }
			}
		);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_card, menu);
		return true;
	}
	
	private void populateForm(ArrayList<String> data)
	{
		linkedin.setText(data.get(5));
		jobDescription.setText(data.get(1));
		phone.setText(data.get(2));
		website.setText(data.get(4));
		email.setText(data.get(3));
	}
	
	public void updateForm(View view)
	{
		String linkedIn = linkedin.getText().toString();
		String jobDesc = jobDescription.getText().toString();
		String phoneNum = phone.getText().toString();
		String web = website.getText().toString();
		String emailAddress = email.getText().toString();
		
		RequestParams params = new RequestParams();
		params.put("user_id", userID);
		params.put("linkedin", linkedIn);
		params.put("job_title", jobDesc);
		params.put("phone", phoneNum);
		params.put("website", web);
		params.put("email", emailAddress);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/cards/updateCard.php",
			params,
			new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			       if (response.equals("true"))
			       {
			    	   Intent intent = new Intent(EditCardActivity.this, MainActivity.class);
			    	   startActivity(intent);
			       }
			       else
			       {
			    	   Toast.makeText(EditCardActivity.this,
			    			   "Could not connect to the internet.", 
			    			   Toast.LENGTH_SHORT).show();
			       }
			    }
			}
		);
	}

}
