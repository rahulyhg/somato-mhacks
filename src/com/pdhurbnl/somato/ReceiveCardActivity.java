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

public class ReceiveCardActivity extends Activity {
	
	private String jsonResponse;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receive_card);
		
		Intent intent = getIntent();
		String card_id = intent.getStringExtra("sender_id");
		
		pullCard(card_id);
        
		
	}

	
	
	private void pullCard(String card_id)
	{
		RequestParams params = new RequestParams();
		params.put("card_id", card_id);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/cards/pullCardById.php",
			params,
			new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			        ReceiveCardActivity.this.jsonResponse = response;
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
		        
		        TextView name = (TextView)findViewById(R.id.name);
		    	TextView jobPos = (TextView)findViewById(R.id.jobPos);
		    	TextView  phone = (TextView)findViewById(R.id.phone);
		    	TextView email = (TextView)findViewById(R.id.email);
		    	TextView website = (TextView)findViewById(R.id.website);
		    	TextView linkedin = (TextView)findViewById(R.id.linkedin);
		        
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
						"Could not get card.",
						Toast.LENGTH_LONG).show();
		    }
		    
		    
	}

}
