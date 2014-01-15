package de.medienDresden.illumina.pilight;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Device implements Parcelable {

    public static final String VALUE_ON = "on";

    public static final String VALUE_OFF = "off";

    public static final int DIM_LEVEL_MAX = 15;

    public static final int DIM_LEVEL_MIN = 0;

    public static final int TYPE_SWITCH = 0;

    public static final int TYPE_DIMMER = 1;

    public static final int TYPE_WEATHER = 2;

    public static final int PROPERTY_DIM_LEVEL = 1;

    public static final int PROPERTY_VALUE = 2;

    private String mId;

    private String mLocationId;

    private String mName;

    private int mOrder;

    private String mValue;

    private int mType = TYPE_SWITCH;

    private int mDimLevel;

    private int mTemperature;

    private boolean mShowTemperatur;

    private int mHumidity;

    private boolean mShowHumidity;

    private int mDecimals;

    public Device() {}

    public String getLocationId() {
        return mLocationId;
    }

    public void setLocationId(String locationId) {
        mLocationId = locationId;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

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

    public void setDimLevel(int dimLevel) {
        mDimLevel = dimLevel;
    }

    public int getDimLevel() {
        return mDimLevel;
    }

    public int getTemperature() {
        return mTemperature;
    }

    public void setTemperature(int temperature) {
        mTemperature = temperature;
    }

    public boolean isShowTemperatur() {
        return mShowTemperatur;
    }

    public void setShowTemperatur(boolean showTemperatur) {
        mShowTemperatur = showTemperatur;
    }

    public int getHumidity() {
        return mHumidity;
    }

    public void setHumidity(int humidity) {
        mHumidity = humidity;
    }

    public boolean showHumidity() {
        return mShowHumidity;
    }

    public void setShowHumidity(boolean showHumidity) {
        mShowHumidity = showHumidity;
    }

    public int getDecimals() {
        return mDecimals;
    }

    public void setDecimals(int decimals) {
        mDecimals = decimals;
    }

    public boolean isOn() {
        return TextUtils.equals(mValue, VALUE_ON);
    }

    public static final Parcelable.Creator<Device> CREATOR
            = new Parcelable.Creator<Device>() {

        @Override
        public Device createFromParcel(Parcel parcel) {
            return new Device(parcel);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }

    };

    public Device(Parcel parcel) {
        mId = parcel.readString();
        mLocationId = parcel.readString();
        mName = parcel.readString();
        mOrder = parcel.readInt();
        mValue = parcel.readString();
        mType = parcel.readInt();
        mDimLevel = parcel.readInt();
        mTemperature = parcel.readInt();
        mHumidity = parcel.readInt();
        mDecimals = parcel.readInt();
        mShowTemperatur = Boolean.parseBoolean(parcel.readString());
        mShowHumidity = Boolean.parseBoolean(parcel.readString());
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mId);
        parcel.writeString(mLocationId);
        parcel.writeString(mName);
        parcel.writeInt(mOrder);
        parcel.writeString(mValue);
        parcel.writeInt(mType);
        parcel.writeInt(mDimLevel);
        parcel.writeInt(mTemperature);
        parcel.writeInt(mHumidity);
        parcel.writeInt(mDecimals);
        parcel.writeString(mShowTemperatur ? "true" : "false");
        parcel.writeString(mShowHumidity ? "true" : "false");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return mName;
    }

}
