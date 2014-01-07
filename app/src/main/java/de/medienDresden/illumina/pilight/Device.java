package de.medienDresden.illumina.pilight;

import java.util.ArrayList;

public class Device {

    public static final int TYPE_SWITCH = 1;

    private String mName;

    private int mOrder;

    private String mValue;

    private ArrayList<String> mValues;

    private int mType;

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public ArrayList<String> getValues() {
        return mValues;
    }

    public void setValues(ArrayList<String> values) {
        mValues = values;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

}
