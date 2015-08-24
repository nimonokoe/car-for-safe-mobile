package com.example.ryosoga.myapplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import com.ibm.mobile.services.core.IBMBluemix;
import com.ibm.mobile.services.data.IBMData;
/**
 * Created by ryosoga on 15/08/13.
 */
public class BluemixConnection extends Application {
    private static final String APP_ID = "082605bb-fd49-4a4d-99c4-5847b48cd787";
    private static final String APP_SECRET = "fe98babda2d17649018dfed8f8cc265385a6c6cf";
    private static final String APP_ROUTE = "http://carforsafemobile.mybluemix.net";
    public static final int EDIT_ACTIVITY_RC = 1;
    private static final String CLASS_NAME = "BluemixConnection";
    List<ItemCompany> itemList;
    List<ItemLocation> itemLocationList;
    public BluemixConnection() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {
                Log.d(CLASS_NAME,
                        "Activity created: " + activity.getLocalClassName());
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.d(CLASS_NAME,
                        "Activity started: " + activity.getLocalClassName());
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.d(CLASS_NAME,
                        "Activity resumed: " + activity.getLocalClassName());
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity,
                                                    Bundle outState) {
                Log.d(CLASS_NAME,
                        "Activity saved instance state: "
                                + activity.getLocalClassName());
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.d(CLASS_NAME,
                        "Activity paused: " + activity.getLocalClassName());
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.d(CLASS_NAME,
                        "Activity stopped: " + activity.getLocalClassName());
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(CLASS_NAME,
                        "Activity destroyed: " + activity.getLocalClassName());
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        itemList = new ArrayList<ItemCompany>();
        itemLocationList = new ArrayList<ItemLocation>();
        // Read from properties file.
        Properties props = new java.util.Properties();
        Context context = getApplicationContext();
        // Initialize the IBM core backend-as-a-service.
        IBMBluemix.initialize(this, APP_ID, APP_SECRET, APP_ROUTE);
        // Initialize the IBM Data Service.
        IBMData.initializeService();
        // Register the Item Specialization.
        ItemCompany.registerSpecialization(ItemCompany.class);
        ItemCompany.registerSpecialization(ItemLocation.class);
    }

    /**
     * returns the itemList, an array of Item objects.
     *
     * @return itemList
     */
    public List<ItemCompany> getItemCompanyList() {
        return itemList;
    }

    public List<ItemLocation> getItemLocationList(){
        return itemLocationList;
    }


}
