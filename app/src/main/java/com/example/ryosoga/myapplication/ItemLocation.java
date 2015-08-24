package com.example.ryosoga.myapplication;
import com.ibm.mobile.services.data.IBMDataObject;

import com.ibm.mobile.services.data.IBMDataObjectSpecialization;

/**
 * Created by ryosoga on 15/08/13.
 */
@IBMDataObjectSpecialization("ItemLocation")
public class ItemLocation extends IBMDataObject {
    public static final String CLASS_NAME = "ItemLocation";
    private static final String LAT = "lat";
    private static final String LNG = "lng";

    public void setLocation(Double lat, Double lng){
        setObject(LAT, lat);
        setObject(LNG, lng);
    }
}
