package de.medienDresden.illumina.pilight;

import java.util.ArrayList;
import java.util.Comparator;

public class Device {

    public enum Type {
        Switch,
        Dimmer
    }

    private String mName;

    private int mOrder;

    private String mValue;

    private ArrayList<String> mValues;

    private Type mType = Type.Switch;

    private int mDimLevel;

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

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDimLevel(int dimLevel) {
        mDimLevel = dimLevel;
    }

    public int getDimLevel() {
        return mDimLevel;
    }

    public static class OrderComparator implements Comparator<Device> {

        @Override
        public int compare(Device d1, Device d2) {
            if (d1.getOrder() > d2.getOrder()) {
                return -1;
            } else if (d1.getOrder() < d2.getOrder()) {
                return 1;
            } else {
                return 0;
            }
        }

    }

}
