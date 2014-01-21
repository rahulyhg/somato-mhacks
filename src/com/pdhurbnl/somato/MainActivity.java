package com.pdhurbnl.somato;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends Activity implements LocationListener {
	
	private String userID;
	private LocationManager service;
	private String provider;
	private LocationManager locationManager;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private Context context;
	private final static String TAG = "Somato";
	
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    
    String SENDER_ID = "611145701344";
    
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    
    String regid;
    
    LinearLayout linearLayout;
    float y1, y2;
    float sensitivity = 50;
    
    ImageButton imageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		imageButton = (ImageButton)findViewById(R.id.imageButton);
		
		// Check for login
		checkLogin();
		if (!this.userID.equals("NOT_LOGGED_IN"))
		{
			// User is logged in
			// Prompt to enable GPS
			enableLocation();
			Location location = initialLocation();
			if (location != null)
			{
				onLocationChanged(location);
			}
			
			this.context = getApplicationContext();
			
			// Check for play services apk
			if (checkPlayServices())
			{
				// User has play services
				gcm = GoogleCloudMessaging.getInstance(this);
				regid = getRegistrationId(context);
				
				if (regid.isEmpty())
				{
					registerInBackground();
				}
				
				linearLayout = (LinearLayout)findViewById(R.id.card);
				init();
				requestCard();
			}
			else
			{
				// User needs to get Play Services APK
			}
		}
		else
		{
			// Start login activity
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		}
	}
	
	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		initialLocation();
		this.locationManager.requestLocationUpdates(this.provider, 2000, 35, this);
		checkPlayServices();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/* ******************************************************************** */
	
	private void init()
	{
        linearLayout.setBackgroundResource(R.drawable.good_theme);
        final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        linearLayout.setOnTouchListener( new OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent touchevent){

                ClipData clipData = ClipData.newPlainText("", "");
                View.DragShadowBuilder dsb = new View.DragShadowBuilder(linearLayout);

                switch (touchevent.getAction()){
                    // when user first touches the screen we get y1, y2 coordinates
                    case MotionEvent.ACTION_DOWN:{
                        y1 = touchevent.getY();

                        linearLayout.startDrag(clipData, dsb, linearLayout, 1);

                        break;
                    } 
                }

                return true;
            }
        });
	
        linearLayout.setOnDragListener( new View.OnDragListener() {

	        @Override
	        public boolean onDrag(View v, DragEvent event) {
	            switch (event.getAction()) {
	
	                case DragEvent.ACTION_DRAG_EXITED:
	                    if ((y1 - y2) > sensitivity){
	                        linearLayout.setVisibility(View.INVISIBLE);
	
	                        vib.vibrate(50);
	                        Toast.makeText(MainActivity.this,
	                        		"Finding nearby users...", Toast.LENGTH_SHORT)
	                        		.show();
	                        
	                        // Request nearby users here
	                        requestNearbyUsers();
	                    }
	                    else
	                    {
	                        linearLayout.setVisibility(View.VISIBLE);
	                    }
	                    break;
	                case DragEvent.ACTION_DROP:
	                   // Toast.makeText(getApplicationContext(), "Drop", Toast.LENGTH_LONG).show();
	                    if ((y1 - y2) > sensitivity){
	                        linearLayout.setVisibility(View.VISIBLE);
	                    }else{
	                        linearLayout.setVisibility(View.INVISIBLE);
	                    }
	
	                    break;
	
	                default:
	                    break;
	            }
	            return true;
	        }
	    });
	}
	
	public void requestCard()
	{
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
						
						MainActivity.this.setChildrenText(data);
					}
			    	catch (JSONException e)
			    	{
						Toast.makeText(MainActivity.this,
								e.getMessage(),
								Toast.LENGTH_LONG).show();
					}
			    }
			}
		);
	}
	
	public void setChildrenText(ArrayList<String> data){

        TextView name = (TextView)findViewById(R.id.name);
        TextView jobPos = (TextView)findViewById(R.id.jobPos);
        TextView phone = (TextView)findViewById(R.id.phone);
        TextView email = (TextView)findViewById(R.id.email);
        TextView website = (TextView)findViewById(R.id.website);
        TextView linkedin = (TextView)findViewById(R.id.linkedin);

        name.setText(data.get(0));
        jobPos.setText(data.get(1));
        phone.setText(data.get(2));
        email.setText(data.get(3));
        website.setText(data.get(4));
        linkedin.setText(data.get(5));
        
        String theme = data.get(6);
        if (theme.equals("LINES"))
        {
        	linearLayout.setBackgroundResource(R.drawable.blackactuallybig);
        }
        else if (theme.equals("DEFAULT"))
        {
        	linearLayout.setBackgroundResource(R.drawable.good_theme);
        }
    }
	
	public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout:
                        logout();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.edit:
                        Intent intent2 = new Intent(MainActivity.this, EditCardActivity.class);
                        startActivity(intent2);
                        return true;
                    case R.id.view:
                        Toast.makeText(getApplicationContext(), "veiwing...", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "default...", Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });
    }
	
	/* ***************************************************************** */
	
	/*
	 * Location services
	 */
	
	private void checkLogin()
	{
		// Get shared preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		this.userID = prefs.getString("user_id", "NOT_LOGGED_IN");
	}
	
	private void logout()
	{
		// Get shared preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove("user_id");
		editor.commit();
	}
	
	private void enableLocation()
	{
		this.service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = this.service.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!enabled)
		{
		  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		  startActivity(intent);
		} 
	}
	
	private Location initialLocation()
	{
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		this.provider = this.locationManager.getBestProvider(criteria, false);
		Location location = this.locationManager.getLastKnownLocation(this.provider);
		return location;
	}
	
	private void putLocation(Location location)
	{
		RequestParams params = new RequestParams();
		params.put("user_id", userID);
		params.put("latitude", String.valueOf(location.getLatitude()));
		params.put("longitude", String.valueOf(location.getLongitude()));
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/user/updateLocation.php",
			params,
			new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			        
			    }
			}
		);
	}

	@Override
	public void onLocationChanged(Location location) {
		putLocation(location);		
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this,
				"Lost location signal.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this,
				"Found a location signal.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Toast.makeText(this,
				"Location services were disabled.",
				Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * End Location services
	 */
	
	/* ********************************************************************* */
	
	/*
	 * Play Services
	 */
	
	private boolean checkPlayServices()
	{
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS)
		{
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
			{
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
			else
			{
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}
	
	// Get a registration id
	private String getRegistrationId(Context context)
	{
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty())
		{
			Log.i(TAG, "Registration not found.");
			return "";
		}
		
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		
		if (registeredVersion != currentVersion)
		{
			Log.i(TAG, "App version changed.");
			return "";
		}
		
		return registrationId;
	}
	
	// Return shared preferences
	private SharedPreferences getGCMPreferences(Context context)
	{
		return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}
	
	// Return app version
	private static int getAppVersion(Context context)
	{
		try
		{
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		}
		catch (NameNotFoundException e)
		{
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	// Register for a new registration ID if necessary
	private void registerInBackground()
	{
		new AsyncTask<Void,Void,String>()
		{
			@Override
			protected String doInBackground(Void... params)
			{
				String msg = "";
				try
				{
					if (gcm == null)
					{
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered.";
					
					sendRegistrationIdToBackend(regid);
					
					storeRegistrationId(context, regid);
				}
				catch (IOException ex)
				{
					msg = "Error: " + ex.getMessage();
				}
				return msg;
			}
		}.execute();
	}
	
	// Update the user's registration id on the server
	private void sendRegistrationIdToBackend(String regId)
	{
		RequestParams params = new RequestParams();
		params.put("user_id", userID);
		params.put("registration_id", regId);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/user/updateRegistrationId.php",
			params,
			new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			        Toast.makeText(MainActivity.this,
			        	"Ready to receive cards.", Toast.LENGTH_SHORT).show();
			    }
			}
		);
	}
	
	// Store the registration id so that we don't always have to register a new one
	private void storeRegistrationId(Context context, String regId)
	{
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regID on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}
	
	/*
	 * End Play services
	 */
	
	/* ***************************************************************** */
	
	/*
	 * Sharing
	 */
	private void requestNearbyUsers()
	{
		RequestParams params = new RequestParams();
		params.put("user_id", userID);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(
			"http://somato.pauldilyard.com/share/requestNearbyUsers.php",
			params,
			new AsyncHttpResponseHandler() {
			    @Override
			    public void onSuccess(String response) {
			    	Intent intent = new Intent(MainActivity.this, SelectShare.class);
			    	intent.putExtra("response", response);
			    	startActivity(intent);
			    }
			}
		);
	}

}
