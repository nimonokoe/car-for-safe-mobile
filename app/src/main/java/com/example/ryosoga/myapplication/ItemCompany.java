package com.example.ryosoga.myapplication;

import com.ibm.mobile.services.data.IBMDataObjectSpecialization;

/**
 * Created by ryosoga on 15/08/13.
 */
@IBMDataObjectSpecialization("ItemCompany")
public class ItemCompany extends Item {
    public static final String CLASS_NAME = "ItemCompany";
    private static final String NAME = "company";
    private static final String CHECKED = "checked";

    public boolean getChecked() {
        return (boolean) getObject(CHECKED);
    }
    public String getId(){
        return (String) getObject("objectId");
    }
    /**
     * Sets the name of a list item, as well as calls setCreationTime().
     * @param itemName
     */
    public void setChecked(boolean checked) {
        setObject(CHECKED, checked);
    }


}
