package com.example.ryosoga.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.mobile.services.data.IBMDataException;
import com.ibm.mobile.services.data.IBMDataObject;
import com.ibm.mobile.services.data.IBMQuery;
import com.squareup.seismic.ShakeDetector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bolts.Continuation;
import bolts.Task;


public class MainActivity extends ActionBarActivity implements ShakeDetector.Listener{
    // with Bluemix
//    public List<ItemCompany> companyItemList;
//    public List<ItemSelectedCompany> companyItemSelectedList;
    public BluemixConnection bluemixConnection;
    public List<ItemLocation> locationList;
//    public ArrayAdapter<ItemCompany> arrayAdapter;
//    public ArrayAdapter<ItemSelectedCompany> arraySelectedAdapter;
    private MyMqttClient mClient;

    // original
    public String CLASS_NAME = "MAIN_ACTIVITY";
    private String CLIENT_NAME = "Ryo Soga";
    private int shake_counter = 0;
    private LocationManager locationManager;
    private final int MIN_MS = 3000;
    private final int MIN_DISTANCE = 10;
    private TextView textViewLocationInformation;
    private SharedPreferences prefs;
    private boolean is_location_listening=false;
    private Time time = new Time("Asia/Tokyo");
    Button initializeButton;

    private String getNowTimeString(){
        time.setToNow();
        return time.hour+":"+time.minute+":"+time.second;
    }

    private void setNotification(){
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.ic_directions_car_white_18dp, getString(R.string.app_name), System.currentTimeMillis());
        Intent intent = new Intent(this, MainActivity.class);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        PendingIntent contextIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name),
                getString(R.string.notify_summary), contextIntent);
        notificationManager.notify(R.string.app_name, notification);
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String msg = "Lat=" + location.getLatitude()
                    + "\nLng=" + location.getLongitude();
            createItem(location.getLatitude(), location.getLongitude());
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("name", CLIENT_NAME);
            hashMap.put("lat", String.valueOf(location.getLatitude()));
            hashMap.put("lng", String.valueOf(location.getLongitude()));
            hashMap.put("time", String.valueOf(getNowTimeString()));

            mClient.publish("walker-location", hashMap);
            Log.d("GPS", msg);
            textViewLocationInformation.setText(msg);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void unregisterLocationListener(){
        locationManager.removeUpdates(locationListener);
    }
    private void setLocationListner(){
        String gpsStatus = android.provider.Settings.Secure
                .getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.d("GPS-status", (gpsStatus.length()==0)?"null":gpsStatus);
        if(gpsStatus.length()==0)return;

        String provider = LocationManager.GPS_PROVIDER;
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                textViewLocationInformation.setText("位置情報取得をONにしてください。");
                return;
            }
            provider = LocationManager.NETWORK_PROVIDER;
            textViewLocationInformation.setText("NETWORK");
        }else{
            textViewLocationInformation.setText("GPS");
        }
        locationManager.requestLocationUpdates(
                provider,
                MIN_MS,
                MIN_DISTANCE,
                locationListener
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        final Intent intent = new Intent(MainActivity.this, ScreenStateService.class);
        final Intent intent_switcher = new Intent(MainActivity.this, Sample.class);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d("MAIN", (prefs.getBoolean("checkbox_location", false))?"true":"false");

        bluemixConnection = (BluemixConnection) getApplication();
        locationList = bluemixConnection.getItemLocationList();
        listLocationItems();
        Set<String> companyList = prefs.getStringSet("company_choose", null);
        if(companyList != null) {
            for (String str : companyList) {
                Log.d("LIST", str);
            }
        }
        setNotification();

//        Button shakeTestButton = (Button)findViewById(R.id.shake_test);
//        shakeTestButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hearShake();
//            }
//        });

        Button button = (Button)findViewById(R.id.to_settings);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent_switcher);
            }
        });

        initializeButton = (Button)findViewById(R.id.location_stop);
        initializeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_location_listening = false;
                unregisterLocationListener();
                deleteItemLocation();
                textViewLocationInformation.setText("Safe");
                initializeButton.setEnabled(false);
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("name", CLIENT_NAME);
                hashMap.put("time", "safe");
                mClient.publish("walker-location", hashMap);
            }
        });
        initializeButton.setEnabled(false);


        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector shakeDetector = new ShakeDetector(this);
        shakeDetector.start(sensorManager);

        textViewLocationInformation = (TextView)findViewById(R.id.locationDebug);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        bluemixConnection = (BluemixConnection) getApplicationContext();
        locationList = bluemixConnection.getItemLocationList();

        mClient = new MyMqttClient(this);
        mClient.connect();
    }

    public void hearShake(){
//        Toast.makeText(this, shake_counter + " Don't shake me, bro!", Toast.LENGTH_SHORT).show();
        if(!is_location_listening){
            deleteItemLocation();
            setLocationListner();
            is_location_listening = true;
            initializeButton.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void createItem(Double lat, Double lng) {
        ItemLocation item = new ItemLocation();
        item.setLocation(lat, lng);
        // Use the IBMDataObject to create and persist the Item object.
        item.save().continueWith(new Continuation<IBMDataObject, Void>() {

            @Override
            public Void then(Task<IBMDataObject> task) throws Exception {
                // Log if the save was cancelled.
                if (task.isCancelled()) {
                    Log.e(CLASS_NAME, "Exception : Task " + task.toString() + " was cancelled.");
                }
                // Log error message, if the save task fails.
                else if (task.isFaulted()) {
                    Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                }
                else{
                    listLocationItems();
                }
                return null;
            }

        });

    }

    public void deleteItemLocation(){
        for (ItemLocation il : locationList){
            deleteItem(il);
        }
        locationList.clear();
    }
    public void deleteItem(ItemLocation item) {
        Log.d(CLASS_NAME, "delete");
        // This will attempt to delete the item on the server.
        item.delete().continueWith(new Continuation<IBMDataObject, Void>() {

            @Override
            public Void then(Task<IBMDataObject> task) throws Exception {
                // Log if the delete was cancelled.
                if (task.isCancelled()) {
                    Log.e(CLASS_NAME, "Exception : Task " + task.toString() + " was cancelled.");
                }

                // Log error message, if the delete task fails.
                else if (task.isFaulted()) {
                    Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                } else {
                    Log.d(CLASS_NAME, "delete accomplished.");
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void listLocationItems() {
        Log.d(CLASS_NAME, "enter");

        try {
            IBMQuery<ItemLocation> query = IBMQuery.queryForClass(ItemLocation.class);
            // Query all the Item objects from the server.
            query.find().continueWith(new Continuation<List<ItemLocation>, Void>() {

                @Override
                public Void then(Task<List<ItemLocation>> task) throws Exception {
                    final List<ItemLocation> objects = task.getResult();
                    // Log if the find was cancelled.
                    if (task.isCancelled()){
                        Log.e(CLASS_NAME, "Exception : Task " + task.toString() + " was cancelled.");
                    }
                    // Log error message, if the find task fails.
                    else if (task.isFaulted()) {
                        Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                    }
                    // If the result succeeds, load the list.
                    else {
                        Log.d(CLASS_NAME, "list");

                        // Clear local itemList.
                        // We'll be reordering and repopulating from DataService.
                        locationList.clear();
                        Log.d(CLASS_NAME, new Integer(objects.size()).toString());

                        for(IBMDataObject item:objects) {
                            locationList.add((ItemLocation) item);
                        }
                    }
                    return null;
                }
            },Task.UI_THREAD_EXECUTOR);

        }  catch (IBMDataException error) {
            Log.e(CLASS_NAME, "Exception : " + error.getMessage());
        }
    }

}
