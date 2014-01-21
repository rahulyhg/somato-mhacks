package com.pdhurbnl.somato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SelectShare extends Activity {
	
	private ListView listView;
	private Map<String, String> userKeys = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_share);
		
		// Get json response from prev activity
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			String response = extras.getString("response");
			
			// Parse json
			try
	        {
				JSONArray jArray = new JSONArray(response);
				
				ArrayList<String> users = new ArrayList<String>();
				
				for (int i=0; i < jArray.length(); i++)
				{
				    try {
				        JSONObject oneObject = jArray.getJSONObject(i);
				        // Pulling items from the array
				        String firstName = oneObject.getString("first_name");
				        String lastName = oneObject.getString("last_name");
				        String userID = oneObject.getString("user_id");
				        String name = firstName + " " + lastName + " " + userID;
				        
				        users.add(name);
				        userKeys.put(userID, "name");
				    }
				    catch (JSONException e)
				    {
				    	Toast.makeText(this,
								"Could not find anyone near you.",
								Toast.LENGTH_LONG).show();
				    }
				}
				
				ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, users);
				listView = (ListView) findViewById(R.id.userList);
				listView.setAdapter(adapter);
			} 
	        catch (JSONException e)
	        {
				Toast.makeText(this, 
						"Could not find anyone near you.",
						Toast.LENGTH_LONG).show();
			}
			
			init();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_share, menu);
		return true;
	}
	
	private void init()
	{
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
	            String item = (String) parent.getItemAtPosition(position);
	            findUserId(item);
        }});
	}
	
	private void findUserId(String name)
	{
		String[] parts = name.split(" ");
		String userID = parts[2];
		sendCard(userID);
	}
	
	private void sendCard(String receiverID)
	{
		// Get shared preferences
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    String senderID = sharedPreferences.getString("user_id", "NOT_LOGGED_IN");
	    
		RequestParams params = new RequestParams();
		params.put("receiver_id", receiverID);
		params.put("sender_id", senderID);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/share/sendToUser.php",
			params,
			new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			    	Intent intent = new Intent(SelectShare.this, MainActivity.class);
			    	startActivity(intent);
			    }
			}
		);
	}

}
