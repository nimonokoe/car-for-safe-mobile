package com.example.ryosoga.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.ibm.mobile.services.data.IBMDataException;
import com.ibm.mobile.services.data.IBMDataObject;
import com.ibm.mobile.services.data.IBMQuery;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bolts.Continuation;
import bolts.Task;

/**
 * A placeholder fragment containing a simple view.
 */
public class SampleFragment extends PreferenceFragment {
    CheckBoxPreference checkBoxBasicPreference,
                    checkBoxBasicPreferenceNetwork,
                    checkBoxBasicPreferenceGPS;
    MultiSelectListPreference multiSelectListPreferenceCompany;
    String originalItem;
    String CLASS_NAME = "Prefrence";
    BluemixConnection blApplication;
    List<ItemCompany> itemCompanyList;

    public SampleFragment() {
    }

    private String getLocationProviderStatus(){
//        String gpsStatus = android.provider.Settings.Secure
//                .getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
        String gpsStatus = android.provider.Settings.Secure
                .getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        return gpsStatus;
    }

    private void setBasicSettings(){
        String status = getLocationProviderStatus();
        Log.d("GPS", status);
        if(status.length() == 0){
            checkBoxBasicPreference.setChecked(false);
            checkBoxBasicPreferenceNetwork.setChecked(false);
            checkBoxBasicPreferenceGPS.setChecked(false);
            return;
        }
        checkBoxBasicPreference.setChecked(true);
        if(status.indexOf("gps") != -1) checkBoxBasicPreferenceGPS.setChecked(true);
        else checkBoxBasicPreferenceGPS.setChecked(false);
        if(status.indexOf("network") != -1) checkBoxBasicPreferenceNetwork.setChecked(true);
        else checkBoxBasicPreferenceNetwork.setChecked(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        checkBoxBasicPreference = (CheckBoxPreference)findPreference("checkbox_location");
        checkBoxBasicPreferenceNetwork = (CheckBoxPreference)findPreference("checkbox_location_network");
        checkBoxBasicPreferenceGPS = (CheckBoxPreference)findPreference("checkbox_location_gps");
        multiSelectListPreferenceCompany = (MultiSelectListPreference)findPreference("company_choose");
        setBasicSettings();
        checkBoxBasicPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent callGPSSettingIntent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                );
                startActivity(callGPSSettingIntent);
                return false;
            }
        });
        checkBoxBasicPreferenceNetwork.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent callGPSSettingIntent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                );
                startActivity(callGPSSettingIntent);
                return false;
            }
        });
        checkBoxBasicPreferenceGPS.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent callGPSSettingIntent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                );
                startActivity(callGPSSettingIntent);
                return false;
            }
        });

        blApplication = (BluemixConnection) getActivity().getApplicationContext();
        itemCompanyList = blApplication.getItemCompanyList();
//        createItem("k-delivers", true);
        listCompanyItems();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setBasicSettings();
            Log.d(CLASS_NAME, key);
            Set<String> checked = multiSelectListPreferenceCompany.getValues();
            for(int i=0; i<itemCompanyList.size(); i++){
                itemCompanyList.get(i).setChecked(false);
            }
            for (String s : checked) {
                itemCompanyList.get(new Integer(s)).setChecked(true);
            }
            for(ItemCompany ic : itemCompanyList){
                ic.save().continueWith(new Continuation<IBMDataObject, Void>() {

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

                        // If the result succeeds, load the list.
                        else {
                            listCompanyItems();
                        }
                        return null;
                    }

                });
            }

//            listCompanyItems();
        }
    };
    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
       setBasicSettings();
    }


    /**
     * Refreshes itemList from data service.
     *
     * An IBMQuery is used to find all the list items.
     */
    public void listCompanyItems() {
        Log.d(CLASS_NAME, "enter");

        try {
            IBMQuery<ItemCompany> query = IBMQuery.queryForClass(ItemCompany.class);
            // Query all the Item objects from the server.
            query.find().continueWith(new Continuation<List<ItemCompany>, Void>() {

                @Override
                public Void then(Task<List<ItemCompany>> task) throws Exception {
                    final List<ItemCompany> objects = task.getResult();
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
                        itemCompanyList.clear();
                        Log.d(CLASS_NAME, new Integer(objects.size()).toString());

                        for(IBMDataObject item:objects) {
                            itemCompanyList.add((ItemCompany) item);
                        }

//                        lvArrayAdapter.notifyDataSetChanged();
                        CharSequence[] css = new CharSequence[itemCompanyList.size()];
                        CharSequence[] css_values = new CharSequence[itemCompanyList.size()];
                        Set<String> checked = new HashSet<String>();
                        for(int i=0; i<itemCompanyList.size(); i++){
                            Log.d(CLASS_NAME, itemCompanyList.get(i).toString());
                            css[i] = itemCompanyList.get(i).toString();
                            css_values[i] = new Integer(i).toString();
                            if(itemCompanyList.get(i).getChecked()){
                                checked.add(new Integer(i).toString());
                                Log.d(CLASS_NAME, new Integer(i).toString());
                            }
                        }
                        multiSelectListPreferenceCompany.setEntries(css);
                        multiSelectListPreferenceCompany.setEntryValues(css_values);
                        multiSelectListPreferenceCompany.setValues(checked);
                    }
                    return null;
                }
            },Task.UI_THREAD_EXECUTOR);

        }  catch (IBMDataException error) {
            Log.d(CLASS_NAME, "test");
            Log.e(CLASS_NAME, "Exception : " + error.getMessage());
        }
    }

    public void createItem(String toAdd, boolean checked) {
        ItemCompany item = new ItemCompany();
//        Item item = new Item();
        if (!toAdd.equals("")) {
            item.setName(toAdd);
            item.setChecked(checked);
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

                    // If the result succeeds, load the list.
                    else {
                        listCompanyItems();
                    }
                    return null;
                }

            });

        }
    }

}
