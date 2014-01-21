package com.pdhurbnl.somato;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ViewCardActivity extends Activity {
	
	private String jsonResponse;
	private TextView name = (TextView)findViewById(R.id.name);
	private TextView jobPos = (TextView)findViewById(R.id.jobPos);
	private TextView  phone = (TextView)findViewById(R.id.phone);
	private TextView email = (TextView)findViewById(R.id.email);
	private TextView website = (TextView)findViewById(R.id.website);
	private TextView linkedin = (TextView)findViewById(R.id.linkedin);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_card);
		
		Intent intent = getIntent();
		String sender_id = intent.getStringExtra("sender_id");
		
		pullCard(sender_id);
        
		
	}

	
	
	private void pullCard(String sender_id)
	{
		RequestParams params = new RequestParams();
		params.put("user_id", sender_id);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/cards/pullCard.php",
			params,
			new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			        ViewCardActivity.this.jsonResponse = response;
			        parseJSONString();
			    }
			}
		);
	}
	
	public void parseJSONString(){
		

		    try {
		        JSONObject oneObject =  new JSONObject(jsonResponse);
		        // Pulling items from the array
		        String firstName = oneObject.getString("first_name");
		        String lastName = oneObject.getString("last_name");
		        String jjob = oneObject.getString("job_title");
		        String jphone = oneObject.getString("phone");
		        String jwebsite = oneObject.getString("website");
		        String jlinkedin = oneObject.getString("linkedin");
		        String jemail = oneObject.getString("email");
		        String jname = firstName + " " + lastName;
		        
		        name.setText(jname);
		        jobPos.setText(jjob);
		        phone.setText(jphone);
		        email.setText(jemail);
		        website.setText(jwebsite);
		        linkedin.setText(jlinkedin);
		        
		        
		    }
		    catch (JSONException e)
		    {
		    	Toast.makeText(this,
						"Could not find anyone near you.",
						Toast.LENGTH_LONG).show();
		    }
		    
		    
	}

}
